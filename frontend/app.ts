document
  .getElementById("defaultImageUpload")
  ?.addEventListener("change", function (e) {
    var reader = new FileReader();
    reader.onload = function (e) {
      const imagePreview = document.getElementById(
        "defaultImagePreview"
      ) as HTMLImageElement;
      imagePreview.src = e.target?.result as string;
    };
    reader.readAsDataURL(e.target?.files[0]);

    // upload
    const formData = new FormData();
    const file = (e.target as HTMLInputElement)?.files?.[0]; // Add null check
    if (file) {
      console.log("file exists");
      formData.append("file", file);
      fetch("/api/upload", {
        method: "POST",
        body: formData,
      })
        .then((response) => response.blob())
        .then((blob) => {
          viewPdf(blob, "defaultView");
        });
    }
  });

document
  .getElementById("rotateImageUpload")
  ?.addEventListener("change", function (e) {
    var reader = new FileReader();
    reader.onload = function (e) {
      const imagePreview = document.getElementById(
        "rotateImagePreview"
      ) as HTMLImageElement;
      imagePreview.src = e.target?.result as string;
    };
    reader.readAsDataURL(e.target?.files[0]);

    // upload
    const formData = new FormData();
    const file = (e.target as HTMLInputElement)?.files?.[0]; // Add null check
    if (file) {
      console.log("file exists");
      formData.append("file", file);
      fetch("/api/upload-rotate", {
        method: "POST",
        body: formData,
      })
        .then((response) => response.blob())
        .then((blob) => {
          viewPdf(blob, "rotateView");
        });
    }
  });

const viewPdf = (blob: Blob, pdfViewId: string) => {
  const pdfUrl = URL.createObjectURL(blob);
  const objectTag = document.createElement("object");

  objectTag.data = pdfUrl;
  objectTag.type = "application/pdf";
  objectTag.width = "600";
  objectTag.height = "400";

  const pdf = document.getElementById(pdfViewId);
  pdf?.appendChild(objectTag);
};
