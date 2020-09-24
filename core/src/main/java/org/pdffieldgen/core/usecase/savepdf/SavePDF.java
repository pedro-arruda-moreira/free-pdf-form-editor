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



package org.pdffieldgen.core.usecase.savepdf;

import java.io.IOException;

import org.pdffieldgen.core.port.IPDFManipulator;
import org.pdffieldgen.core.usecase.exception.UseCaseException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavePDF implements ISavePDF {
  
  private final IPDFManipulator pdfManipulator;

  @Override
  public Response execute(Request in) throws UseCaseException {
    try {
      return new Response(pdfManipulator.savePdf(in.getUuid(), in.getLocation()));
    } catch (IOException e) {
      throw new UseCaseException(e);
    }
  }

}
