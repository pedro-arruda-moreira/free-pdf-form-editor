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



package org.pdffieldgen.core.usecase.loadpage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.pdffieldgen.core.port.IPDFManipulator;
import org.pdffieldgen.core.port.IPDFManipulator.PDFField;
import org.pdffieldgen.core.usecase.exception.UseCaseException;
import org.pdffieldgen.core.usecase.loadpage.Response.Field;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoadPage implements ILoadPage {


  private final IPDFManipulator pdfManipulator;

  @Override
  public Response execute(Request in) throws UseCaseException {
    int pageNum = in.getPage();
    UUID pdfId = in.getId();
    InputStream pageImage;
    try {
      pageImage = pdfManipulator.getPage(pdfId, pageNum);
    } catch (IOException e) {
      throw new UseCaseException(e);
    }
    if(pageImage == null) {
      return null;
    }
    Collection<PDFField> fields = pdfManipulator.getFields(pdfId, pageNum);
    return new Response(convert(fields), pageImage);
  }

  private List<Field> convert(Collection<PDFField> fields) {
    ArrayList<Field> arrayList = new ArrayList<>();
    for(PDFField f : fields) {
      arrayList.add(new Field(f.getId(), f.getX(), f.getY(), f.getWidth(),
          f.getHeight(), f.getTooltip(), f.getTooltip(), f.getType()));
    }
    return arrayList;
  }

}
