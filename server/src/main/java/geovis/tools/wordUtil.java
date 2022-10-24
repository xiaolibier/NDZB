package geovis.tools;

import lombok.Data;
import java.io.*;

//import com.datahub.aimindgraph.exception.WordExtractorException;
import fr.opensagres.poi.xwpf.converter.core.FileImageExtractor;
import fr.opensagres.poi.xwpf.converter.core.FileURIResolver;
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLConverter;
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * 内部使用的自研工具方法
 */
@Data
public class wordUtil {


    /**
     * word2007
     */
    public final static String DOCX = ".docx";
    /**
     * word2003
     */
    public final static String DOC = ".doc";

    public static void main(String[] args) {
        File file = new File("D:\\temp\\test.doc");
        File imageFolderFile = new File("D:\\temp\\images\\");
        wordToHtmlString(file, imageFolderFile);
    }

    public static String wordToHtmlString(String filePath, String imageFolderPath) {
        return wordToHtmlString(new File(filePath), new File(imageFolderPath));
    }

    public static String wordToString(String filePath) {
        return wordToString(new File(filePath));
    }

    /**
     * 从word获取带html格式的文本
     * @param file
     * @param imageFolderFile
     * @return
     */
    public static String wordToHtmlString(File file, File imageFolderFile) {
        if (!file.exists()) {
            return "noFile";
            //throw new WordExtractorException("file does not exists!");
        } else {
            if (!imageFolderFile.exists()) {
                imageFolderFile.mkdirs();
            }
            if (file.getName().toLowerCase().endsWith(DOCX)) {
                return word2007ToHtmlString(file, imageFolderFile);
            } else if(file.getName().toLowerCase().endsWith(DOC)){
                return word2003ToHtmlString(file, imageFolderFile);
            } else {
                return "noFile";
                //throw new WordExtractorException("Only doc or docx files are supported");
            }
        }
    }

    /**
     * 从word获取不带格式的文本
     * @param file
     * @return
     */
    public static String wordToString(File file) {
        if (!file.exists()) {
            return "noFile";
            //throw new WordExtractorException("file does not exists!");
        } else {
            if (file.getName().toLowerCase().endsWith(DOCX)) {
                return word2007ToString(file);
            } else if(file.getName().toLowerCase().endsWith(DOC)){
                return word2003ToString(file);
            } else {
                return "noFile";
                //throw new WordExtractorException("Only doc or docx files are supported");
            }
        }
    }

    /**
     * @param wordFile
     * @return
     */
    private static String word2007ToString(File wordFile) {
        try(InputStream in = new FileInputStream(wordFile)) {
            StringBuilder result = new StringBuilder();
            XWPFDocument document = new XWPFDocument(in);
            XWPFWordExtractor re = new XWPFWordExtractor(document);
            result.append(re.getText());
            re.close();
            return result.toString();
        } catch (Exception e) {
            return "noFile";
            //throw new WordExtractorException(e.getMessage());
        }
    }

    /**
     * @param wordFile
     * @return
     */
    private static String word2003ToString(File wordFile) {
        try(InputStream in = new FileInputStream(wordFile)) {
            WordExtractor wordExtractor = new WordExtractor(in);
            return wordExtractor.getText();
        } catch (Exception e) {
            return "noFile";
            //throw new WordExtractorException(e.getMessage());
        }
    }

    /**
     *
     * @param wordFile
     * @param imageFolderFile
     * @return
     */
    private static String word2007ToHtmlString(File wordFile, File imageFolderFile) {
        try (InputStream in = new FileInputStream(wordFile);
             XWPFDocument document = new XWPFDocument(in);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            XHTMLOptions options = XHTMLOptions.create().URIResolver(new FileURIResolver(imageFolderFile));
            options.setExtractor(new FileImageExtractor(imageFolderFile));
            options.setIgnoreStylesIfUnused(false);
            options.setFragment(true);
            XHTMLConverter.getInstance().convert(document, baos, options);
            return baos.toString();
        } catch (Exception e) {
            return "noFile";
            //throw new WordExtractorException(e.getMessage());
        }
    }

    /**
     *
     * @param wordFile
     * @param imageFolderFile
     * @return
     */
    private static String word2003ToHtmlString(File wordFile, File imageFolderFile) {
        String absolutePath = imageFolderFile.getAbsolutePath();
        String imagePath = absolutePath.endsWith(File.separator) ? absolutePath : absolutePath + File.separator;
        try (InputStream input = new FileInputStream(wordFile);
             HWPFDocument wordDocument = new HWPFDocument(input);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStream outStream = new BufferedOutputStream(baos)) {
            WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
            //图片存放的位置
            wordToHtmlConverter.setPicturesManager((content, pictureType, suggestedName, widthInches, heightInches) -> {
                String imageFile = imagePath + suggestedName;
                File file = new File(imageFile);
                try {
                    OutputStream os = new FileOutputStream(file);
                    os.write(content);
                    os.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return imageFile;
            });
            //解析word文档
            wordToHtmlConverter.processDocument(wordDocument);
            Document htmlDocument = wordToHtmlConverter.getDocument();
            DOMSource domSource = new DOMSource(htmlDocument);
            StreamResult streamResult = new StreamResult(outStream);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer serializer = factory.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty(OutputKeys.METHOD, "html");
            serializer.transform(domSource, streamResult);
            return baos.toString();
        } catch (Exception e) {
            return "noFile";
            //throw new WordExtractorException(e.getMessage());
        }
    }




}
