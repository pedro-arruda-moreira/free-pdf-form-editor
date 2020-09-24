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



package org.pdffieldgen.core.usecase.changefield;

import java.io.IOException;
import java.util.stream.Collectors;

import org.pdffieldgen.core.port.IPDFManipulator;
import org.pdffieldgen.core.port.IPDFManipulator.PDFField;
import org.pdffieldgen.core.usecase.exception.UseCaseException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChangeField implements IChangeField {

  private final IPDFManipulator pdfManipulator;
  @Override
  public Response execute(Request in) throws UseCaseException {
    /*try {
      if(pdfManipulator.openPdf(null, null, in.getPdf()) == null) {
        return new Response(null);
      }
    } catch (IOException e) {
      throw new UseCaseException(e);
    }
    PDFField field = pdfManipulator.getFields(in.getPdf(), in.getPage()).stream().filter((f) -> {
      return f.getId().equals(in.getField());
    }).collect(Collectors.toList()).get(0);
    
    switch (in.getType()) {
    case HEIGHT:
      field = field.withHeight(Integer.parseInt(in.getValue().toString()));
      break;
    case NAME:
      field = field.withName(in.getValue().toString());
      break;
    case PAGE:
      field = field.withPage(Integer.parseInt(in.getValue().toString()));
      break;
    case TOOLTIP:
      field = field.withTooltip(in.getValue().toString());
      break;
    case WIDTH:
      field = field.withWidth(Integer.parseInt(in.getValue().toString()));
      break;
    case X:
      field = field.withX(Integer.parseInt(in.getValue().toString()));
      break;
    case Y:
      field = field.withY(Integer.parseInt(in.getValue().toString()));
      break;
    default:
      return new Response(null);
    }
    return new Response(pdfManipulator.updateField(in.getPdf(), field));*/
    return null;
  }

}
