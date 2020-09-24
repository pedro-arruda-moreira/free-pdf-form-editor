/*  free-pdf-form-editor: A free (as in free beer and also as in free speech) PDF form editor!
    Copyright (C) 2020  Pedro de Arruda Moreira

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */



package org.pdffieldgen.core.usecase.formtransfer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.pdffieldgen.core.port.IPDFManipulator;
import org.pdffieldgen.core.port.IPDFManipulator.PDFField;
import org.pdffieldgen.core.usecase.exception.UseCaseException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class FormTransfer implements IFormTransfer {
  private final IPDFManipulator pdfManipulator;

  @Override
  public Response execute(Request in) throws UseCaseException {
    UUID src = null;
    try {
      src = pdfManipulator.openPdf(in.getSource(), null);
      UUID destination = pdfManipulator.openPdf(in.getDestination(), null);

      List<PDFField> srcFields = new ArrayList<>();
      {
        List<PDFField> tmpSrcFields = gatherAllFields(src);
        for(PDFField f : tmpSrcFields) {
          srcFields.add(f.withId(null));
        }
      }
      for(PDFField f : srcFields) {
        pdfManipulator.addField(destination, f);
      }
      return new Response(destination);
    } catch (IOException e) {
      throw new UseCaseException(e);
    } finally {
      if(src != null) {
        pdfManipulator.closePdf(src);
      }
    }
  }

  private List<PDFField> gatherAllFields(UUID pdf) {
    ArrayList<PDFField> allFields = new ArrayList<IPDFManipulator.PDFField>();
    int pages = pdfManipulator.getNumberOfPages(pdf);
    for(int i = 0; i < pages; i++) {
      allFields.addAll(pdfManipulator.getFields(pdf, i));
    }
    return allFields;
  }

}
