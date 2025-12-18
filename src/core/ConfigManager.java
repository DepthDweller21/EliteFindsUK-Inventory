package core;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.nio.file.Paths;

/**
 * Utility class for managing application configuration
 * Reads and writes settings to config.xml
 */
public class ConfigManager {
    private static final String CONFIG_PATH = "src/core/config.xml";
    private static final double DEFAULT_GBP_TO_PKR_RATE = 350.0;
    private static final String DEFAULT_PLATFORM_FEES = "15,20,25";
    
    /**
     * Get the database connection string from config.xml
     * @return Connection string, or null if missing or empty
     */
    public static String getConnectionString() {
        try {
            Document doc = loadConfig();
            NodeList nodeList = doc.getElementsByTagName("connectionString");
            if (nodeList.getLength() > 0) {
                String connectionString = nodeList.item(0).getTextContent().trim();
                return connectionString.isEmpty() ? null : connectionString;
            }
        } catch (Exception e) {
            System.err.println("Error reading connection string: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Get the GBP to PKR exchange rate from config.xml
     * @return Exchange rate, default 350.0 if missing
     */
    public static double getGbpToPkrRate() {
        try {
            Document doc = loadConfig();
            NodeList nodeList = doc.getElementsByTagName("gbpToPkrRate");
            if (nodeList.getLength() > 0) {
                String rateStr = nodeList.item(0).getTextContent().trim();
                if (!rateStr.isEmpty()) {
                    return Double.parseDouble(rateStr);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading exchange rate: " + e.getMessage());
        }
        return DEFAULT_GBP_TO_PKR_RATE;
    }
    
    /**
     * Get platform fees from config.xml
     * @return Comma-separated platform fees string, default "15,20,25" if missing
     */
    public static String getPlatformFees() {
        try {
            Document doc = loadConfig();
            NodeList nodeList = doc.getElementsByTagName("platformFees");
            if (nodeList.getLength() > 0) {
                String fees = nodeList.item(0).getTextContent().trim();
                if (!fees.isEmpty()) {
                    return fees;
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading platform fees: " + e.getMessage());
        }
        return DEFAULT_PLATFORM_FEES;
    }
    
    /**
     * Set the database connection string in config.xml
     * @param connectionString The connection string to save
     */
    public static void setConnectionString(String connectionString) {
        try {
            Document doc = loadConfig();
            Element root = doc.getDocumentElement();
            
            NodeList nodeList = doc.getElementsByTagName("connectionString");
            Element connectionElement;
            if (nodeList.getLength() > 0) {
                connectionElement = (Element) nodeList.item(0);
            } else {
                connectionElement = doc.createElement("connectionString");
                root.appendChild(connectionElement);
            }
            
            connectionElement.setTextContent(connectionString != null ? connectionString : "");
            saveConfig(doc);
        } catch (Exception e) {
            System.err.println("Error setting connection string: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Set the GBP to PKR exchange rate in config.xml
     * @param rate The exchange rate to save
     */
    public static void setGbpToPkrRate(double rate) {
        try {
            Document doc = loadConfig();
            Element root = doc.getDocumentElement();
            
            NodeList nodeList = doc.getElementsByTagName("gbpToPkrRate");
            Element rateElement;
            if (nodeList.getLength() > 0) {
                rateElement = (Element) nodeList.item(0);
            } else {
                rateElement = doc.createElement("gbpToPkrRate");
                root.appendChild(rateElement);
            }
            
            rateElement.setTextContent(String.valueOf(rate));
            saveConfig(doc);
        } catch (Exception e) {
            System.err.println("Error setting exchange rate: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Set platform fees in config.xml
     * @param fees Comma-separated platform fees string (e.g., "15,20,25")
     */
    public static void setPlatformFees(String fees) {
        try {
            Document doc = loadConfig();
            Element root = doc.getDocumentElement();
            
            NodeList nodeList = doc.getElementsByTagName("platformFees");
            Element feesElement;
            if (nodeList.getLength() > 0) {
                feesElement = (Element) nodeList.item(0);
            } else {
                feesElement = doc.createElement("platformFees");
                root.appendChild(feesElement);
            }
            
            feesElement.setTextContent(fees != null ? fees : DEFAULT_PLATFORM_FEES);
            saveConfig(doc);
        } catch (Exception e) {
            System.err.println("Error setting platform fees: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load config.xml document
     * Creates default structure if file doesn't exist
     */
    private static Document loadConfig() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        try {
            Document doc = builder.parse(Paths.get(CONFIG_PATH).toFile());
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            // File doesn't exist or is invalid, create default structure
            Document doc = builder.newDocument();
            Element root = doc.createElement("config");
            doc.appendChild(root);
            
            Element connectionElement = doc.createElement("connectionString");
            connectionElement.setTextContent("");
            root.appendChild(connectionElement);
            
            Element rateElement = doc.createElement("gbpToPkrRate");
            rateElement.setTextContent(String.valueOf(DEFAULT_GBP_TO_PKR_RATE));
            root.appendChild(rateElement);
            
            Element feesElement = doc.createElement("platformFees");
            feesElement.setTextContent(DEFAULT_PLATFORM_FEES);
            root.appendChild(feesElement);
            
            saveConfig(doc);
            return doc;
        }
    }
    
    /**
     * Save config.xml document
     */
    private static void saveConfig(Document doc) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(Paths.get(CONFIG_PATH).toFile());
        transformer.transform(source, result);
    }
}

