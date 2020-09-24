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



package org.pdffieldgen.core.port;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.UUID;

import org.pdffieldgen.core.model.pdf.field.FieldType;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.With;

public interface IPDFManipulator {
  @Getter
  @RequiredArgsConstructor()
  @EqualsAndHashCode(onlyExplicitlyIncluded=true)
  @ToString
  public static class PDFField {
    @EqualsAndHashCode.Include
    @With
    private final UUID id;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final int page;
    private final String tooltip;
    private final String name;
    private final FieldType type;
    private final String checkValue;
    
  }
  
  InputStream getPage(UUID uuid, int page) throws IOException;
  
  Collection<PDFField> getFields(UUID uuid, int page);
  
  UUID updateField(UUID uuid, PDFField fieldData);
  
  UUID savePdf(UUID uuid, String location) throws IOException;

  UUID addField(UUID docId, PDFField fieldData);

  PDFField getField(UUID docId, int page, UUID fieldId);

  UUID openPdf(String location, UUID receivedId) throws IOException;

  UUID closePdf(UUID docId);

  int getNumberOfPages(UUID docId);
  
}
