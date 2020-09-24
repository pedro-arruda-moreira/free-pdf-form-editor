package org.pdffieldgen.core.adapters.port;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.pdffieldgen.core.model.pdf.field.FieldType;
import org.pdffieldgen.core.port.IPDFManipulator;
import org.springframework.stereotype.Service;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfButtonFormField;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.forms.fields.PdfTextFormField;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.annot.PdfWidgetAnnotation;

import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
public class PDFManipulator implements IPDFManipulator {

  @RequiredArgsConstructor
  @ToString
  private static class Pdf {
    private final PDFRenderer pdfBoxRend;
    private final PdfDocument doc;
    private final List<Map<UUID, PDFField>> fields;
    private final OutputStream out;
  }

  private final Map<UUID, Pdf> loadedDocuments = new ConcurrentHashMap<>();

  @Override
  public UUID openPdf(String location, UUID receivedId) throws IOException {
    if(receivedId == null) {
      CloningPdfDocument tmpDoc = new CloningPdfDocument(new PdfReader(new FileInputStream(location)));
      PdfAcroForm tmpForm = loadForm(tmpDoc);
      List<Map<UUID, PDFField>> thePages = loadPages(tmpDoc, tmpForm);
      clearForm(tmpForm);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PdfDocument doc = tmpDoc.getClone(baos);
      UUID newUuid = UUID.randomUUID();
      loadedDocuments.put(newUuid, new Pdf(
          new PDFRenderer(PDDocument.load(tmpDoc.getByteArray())),
          doc,
          thePages,
          baos
          ));
      return newUuid;
    }
    if(loadedDocuments.containsKey(receivedId)) {
      return receivedId;
    }
    return null;
  }

  private void clearForm(PdfAcroForm form) {
    Map<String, PdfFormField> formFields = form.getFormFields();
    List<String> ids = new ArrayList<>();
    for(String id : formFields.keySet()) {
      ids.add(id);
    }
    for(String id : ids) {
      form.removeField(id);
    }
  }

  private List<Map<UUID, PDFField>> loadPages(PdfDocument itextDoc, PdfAcroForm form) throws IOException {
    ArrayList<Map<UUID, PDFField>> pages = new ArrayList<>();
    for(int curPage = 1; curPage <= itextDoc.getNumberOfPages(); curPage++) {
      pages.add(loadFields(form, curPage, itextDoc));
    }
    return pages;
  }

  private Map<UUID, PDFField> loadFields(PdfAcroForm form, int curPage, PdfDocument doc) {
    Map<String, PdfFormField> itextFormFields = form.getFormFields();
    Map<UUID, PDFField> ret = new HashMap<>();
    for(Entry<String, PdfFormField> fieldEntry : itextFormFields.entrySet()) {
      PdfFormField field = fieldEntry.getValue();
      if(field.getFieldName() == null) {
        continue;
      }
      FieldType type = determineType(field);
      addFieldInstances(ret, type, curPage, doc, field);
    }
    return ret;
  }


  private void addFieldInstances(Map<UUID, PDFField> ret,
      FieldType type, int curPage, PdfDocument doc, PdfFormField field) {
    List<PdfWidgetAnnotation> widgets = field.getWidgets();
    for(PdfWidgetAnnotation anno : widgets) {
      if(doc.getPageNumber(anno.getPage()) != curPage) {
        continue;
      }
      Rectangle rect = anno.getRectangle().toRectangle();

      ret.put(UUID.randomUUID(),
          new PDFField(UUID.randomUUID(), rect.getX(), rect.getY(),
              rect.getWidth(), rect.getHeight(), curPage - 1, "", field.getFieldName().toString(), type, determineCheck(field, anno)));
    }
  }

  private String determineCheck(PdfFormField value, PdfWidgetAnnotation anno) {
    if(determineType(value) == FieldType.TEXT) {
      return null;
    }
    PdfDictionary apPart = anno.getPdfObject().getAsDictionary(PdfName.AP);
    if(apPart != null) {
      return getCheckValueFromNPart(apPart.getAsDictionary(PdfName.N));
    }
    return null;
  }

  private String getCheckValueFromNPart(PdfDictionary nPart) {
    if(nPart == null) {
      return null;
    }
    for(PdfName name : nPart.keySet()) {
      String nameAsString = name.getValue();
      if(!PdfName.staticNames.containsKey(nameAsString) && !"Off".equals(nameAsString)) {
        return nameAsString;
      }
    }
    return null;
  }

  private FieldType determineType(PdfFormField pdfFormField) {
    Class<? extends PdfFormField> clazz = pdfFormField.getClass();
    if(clazz == PdfTextFormField.class) {
      return FieldType.TEXT;
    }
    if(clazz == PdfButtonFormField.class) {
      if(((PdfButtonFormField)pdfFormField).isRadio()) {
        return FieldType.RADIO;
      }

      return FieldType.CHECKBOX;
    }
    return null;
  }

  private PdfAcroForm loadForm(PdfDocument itextDoc) {
    return PdfAcroForm.getAcroForm(itextDoc, true);
  }
  @Override
  public int getNumberOfPages(UUID docId) {
    return loadedDocuments.get(docId).doc.getNumberOfPages();
  }

  @Override
  public InputStream getPage(UUID docId, int page) throws IOException {
    PDFRenderer rend = loadedDocuments.get(docId).pdfBoxRend;
    BufferedImage img = rend.renderImage(page);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ImageIO.write(img, "jpg", output);
    return new ByteArrayInputStream(output.toByteArray());
  }

  @Override
  public Collection<PDFField> getFields(UUID docId, int page) {
    return loadedDocuments.get(docId).fields.get(page).values();
  }

  @Override
  public UUID addField(UUID docId, PDFField fieldData) {
    if(fieldData.getId() != null) {
      return null;
    }
    int page = fieldData.getPage();
    if(page >= getNumberOfPages(docId)) {
      log.warn(String.format("ignoring field %s: page %s does not exist.", fieldData.getName(), page));
      return null;
    }
    UUID id = UUID.randomUUID();
    loadedDocuments.get(docId).fields.get(page).put(id, fieldData.withId(id));
    return id;
  }


  @Override
  public PDFField getField(UUID docId, int page, UUID fieldId) {
    return loadedDocuments.get(docId).fields.get(page).get(fieldId);
  }

  @Override
  public UUID updateField(UUID docId, PDFField fieldData) {
    UUID fieldId = fieldData.getId();
    if(fieldId == null) {
      return null;
    }
    for(Map<UUID, PDFField> fieldsInPage : loadedDocuments.get(docId).fields) {
      if(fieldsInPage.containsKey(fieldId)) {
        fieldsInPage.put(fieldId, fieldData.withId(fieldId));
        return fieldId;
      }
    }
    return null;
  }

  @Override
  public UUID closePdf(UUID docId) {
    loadedDocuments.remove(docId).doc.close();
    return docId;
  }

  @Override
  public UUID savePdf(UUID docId, String location) throws IOException {
    Pdf pdf = loadedDocuments.get(docId);
    PdfDocument doc = pdf.doc;
    PdfAcroForm form = PdfAcroForm.getAcroForm(doc, true);
    form.getFieldsForFlattening();
    List<PDFField> allFields = gatherAllFields(pdf);
    for(PDFField field : allFields) {
      putFieldInDocument(field, form, doc);
    }
    doc.close();
    loadedDocuments.remove(docId);
    @Cleanup OutputStream outToFile = new FileOutputStream(new File(location));
    IOUtils.copy(new ByteArrayInputStream(((ByteArrayOutputStream)pdf.out).toByteArray()), outToFile);
    return docId;
  }

  private void putFieldInDocument(PDFField field, PdfAcroForm form, PdfDocument doc) {
    Rectangle rect = new Rectangle(field.getX(), field.getY(), field.getWidth(), field.getHeight());
    int page = field.getPage() + 1;
    String name = field.getName();
    PdfFormField existingField = form.getField(name);
    String checkValue = field.getCheckValue();
    boolean fieldIsNew = existingField == null;
    switch (field.getType()) {
    case TEXT:
      if(fieldIsNew) {
        existingField = PdfFormField.createText(doc, rect, name);
        existingField.setPage(page);
      } else {
        addWidgetToExistingField(doc, rect, page, existingField);
      }
      break;
    case CHECKBOX:
      if(fieldIsNew) {
        existingField = PdfFormField.createCheckBox(doc, rect, name, checkValue);
        existingField.setPage(page);
      } else {
        addWidgetToExistingField(doc, rect, page, existingField);
      }
      break;
    default:
      // RADIO
      if(fieldIsNew) {
        existingField = PdfFormField.createRadioGroup(doc, name, "");
      }

      PdfFormField newRadio = PdfFormField.createRadioButton(doc, rect, (PdfButtonFormField) existingField, checkValue);
      newRadio.setPage(page);
      break;
    }
    if(fieldIsNew) {
      form.addField(existingField);
    }
  }

  private void addWidgetToExistingField(PdfDocument doc, Rectangle rect, int page,
      PdfFormField existingField) {
    PdfWidgetAnnotation widget = new PdfWidgetAnnotation(rect);
    widget.makeIndirect(doc);
    existingField.addKid(widget);
    doc.getPage(page).addAnnotation(widget);
  }

  private List<PDFField> gatherAllFields(Pdf pdf) {
    List<PDFField> allFields = new ArrayList<>();
    for(Map<UUID, PDFField> fieldsInPage : pdf.fields) {
      allFields.addAll(fieldsInPage.values());
    }
    return allFields;
  }

}
