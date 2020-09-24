package org.pdffieldgen.core.adapters.port;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

class CloningPdfDocument extends PdfDocument {
  private final ByteArrayOutputStream baos;

  CloningPdfDocument(PdfReader reader) {
    super(reader, new PdfWriter(new ByteArrayOutputStream()));
    baos = (ByteArrayOutputStream)this.getWriter().getOutputStream();
  }
  
  PdfDocument getClone(OutputStream out) throws IOException {
    this.close();
    return new PdfDocument(new PdfReader(
        new ByteArrayInputStream(baos.toByteArray())), new PdfWriter(out));
  }
  
  byte[] getByteArray() {
    return baos.toByteArray();
  }

}
