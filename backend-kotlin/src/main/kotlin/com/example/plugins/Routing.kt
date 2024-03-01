package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import org.apache.pdfbox.pdmodel.*
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.ByteArrayOutputStream
import java.io.File
import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.net.URLConnection
import javax.imageio.ImageIO


fun Application.configureRouting() {
    routing {
        get("/api") {
            call.respondText("Hello World!")
        }

        // upload image
        post("/api/upload") {
            val multipartData = call.receiveMultipart()

            var fileName = ""
            lateinit var fileBytes: ByteArray

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        fileName = part.originalFileName as String
                        fileBytes = part.streamProvider().readBytes()
                        File("uploads/default-${fileName}").writeBytes(fileBytes)
                    }

                    else -> {}
                }
                part.dispose()
            }
            val pdfBytes = createPdf(fileName, fileBytes)

            call.respondBytes(pdfBytes, ContentType.Application.Pdf)
        }

        post("/api/upload-rotate") {
            val multipartData = call.receiveMultipart()

            var fileName = ""
            lateinit var fileBytes: ByteArray

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        fileName = part.originalFileName as String
                        fileBytes = part.streamProvider().readBytes()
                        fileBytes = getRotateImageByOrientation(fileBytes)
                        File("uploads/rotate-${fileName}").writeBytes(fileBytes)
                    }

                    else -> {}
                }
                part.dispose()
            }
            val pdfBytes = createPdf("rotate-${fileName}", fileBytes)

            call.respondBytes(pdfBytes, ContentType.Application.Pdf)
        }
    }
}

fun createPdf(fileName: String, fileBytes: ByteArray): ByteArray{
    val doc = PDDocument()

    val image = PDImageXObject.createFromByteArray(doc, fileBytes, fileName)
    val imageWidth = image.width.toFloat()
    val imageHeight = image.height.toFloat()

    val pageSize = PDRectangle(imageWidth, imageHeight)
    val firstPage = PDPage(pageSize)
    doc.addPage(firstPage)

    PDPageContentStream(doc, firstPage).use {
        it.drawImage(image, 0f, 0f, imageWidth, imageHeight)
    }

    val outputStream = ByteArrayOutputStream()
    doc.save(outputStream)
    val docBytes: ByteArray = outputStream.toByteArray()

    // save pdf to local
    File("uploads/default-${fileName}.pdf").writeBytes(docBytes)

    return docBytes

}



fun getRotateImageByOrientation(byteImage: ByteArray): ByteArray {
    val image: BufferedImage = ImageIO.read(ByteArrayInputStream(byteImage))
    val mimeType = URLConnection.guessContentTypeFromStream(ByteArrayInputStream(byteImage))

    val metadata = Imaging.getMetadata(byteImage)
    val jpegMetadata = metadata as JpegImageMetadata
    val exif = jpegMetadata.exif
    val orientationField = exif.findField(TiffTagConstants.TIFF_TAG_ORIENTATION)
    val orientation = (orientationField?.value as? Short)?.toInt()


    val angle = when (orientation) {
        6 -> 90.0
        8 -> -90.0
        3 -> 180.0
        else -> 0.0
    }

    val transform = AffineTransform.getRotateInstance(Math.toRadians(angle), image.width / 2.0, image.height / 2.0)
    val op = AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR)

    val rotatedImage = BufferedImage(image.width, image.height, image.type)
    op.filter(image, rotatedImage)

    val outputStream = ByteArrayOutputStream()
    ImageIO.write(rotatedImage, getFileType(mimeType), outputStream)
    val rotatedByteImage: ByteArray = outputStream.toByteArray()

    return rotatedByteImage

}


fun getFileType(mimeType: String): String {
    return when (mimeType) {
        "image/png" -> "png"
        "image/jpeg" -> "jpg"
        "application/pdf" -> "pdf"
        else -> "unknown"
    }
}