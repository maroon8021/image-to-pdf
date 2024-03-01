import fastify from "fastify";
import fastifyMultipart from "@fastify/multipart";
import fs from "fs";
import { PDFDocument } from "pdf-lib";
import sharp from "sharp";

const exifremove = require("exifremove");

const server = fastify();
server.register(fastifyMultipart, {
  limits: {
    fileSize: 100 * 1024 * 1024, // 100MB
  },
});

server.listen({ port: 51111 });

server.post("/api/upload", async (request, reply) => {
  try {
    const data = await request.file();
    if (!data) {
      reply.status(400).send("No file uploaded");
    }
    const rawbuffer = await data?.toBuffer();
    if (!rawbuffer) {
      throw new Error("no buffer");
    }

    const buffer = exifremove.remove(rawbuffer);
    const fileType = getFileType(data?.mimetype as string);

    const rotatedImage = await sharp(rawbuffer).rotate().toBuffer();

    if (buffer) {
      fs.writeFileSync(`image.${fileType}`, buffer);
      fs.writeFileSync(`rotated-image.${fileType}`, rotatedImage);
    } else {
      reply.status(400).send("No file uploaded");
      return;
    }

    // to pdf
    const pdfDoc = await PDFDocument.create();
    const image = await getEmbedImage(pdfDoc, rotatedImage, fileType);
    const page = pdfDoc.addPage([image.width, image.height]);
    page.drawImage(image, {
      x: 0,
      y: 0,
      width: image.width,
      height: image.height,
    });
    const pdfBytes = await pdfDoc.save();

    fs.writeFileSync("imagepdf.pdf", pdfBytes);
    reply.header("Content-Type", "application/pdf");
    reply.send(pdfBytes);
  } catch (error) {
    console.log(error);
  }
});

const getFileType = (mineType: string) => {
  switch (mineType) {
    case "image/png":
      return "png";
    case "image/jpeg":
      return "jpg";
    case "application/pdf":
      return "pdf";
    default:
      return "unknown";
  }
};

const getEmbedImage = async (
  pdfDoc: PDFDocument,
  buffer: Buffer,
  fileType: string
) => {
  switch (fileType) {
    case "png":
      return await pdfDoc.embedPng(buffer);
    case "jpg":
      return await pdfDoc.embedJpg(buffer);
    default:
      throw new Error("not supported file type");
  }
};
