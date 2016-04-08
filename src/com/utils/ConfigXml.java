package com.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;    
import javax.xml.parsers.DocumentBuilderFactory;    
import javax.xml.parsers.ParserConfigurationException;    
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;    
import org.w3c.dom.Element;    
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;    
import org.xml.sax.SAXException;    
import android.annotation.SuppressLint;
import android.content.Context;

/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2014-7-16 上午11:54:47
 * To change this template use File | Settings | File Templates.
 * Description：解析XML
 */
public class ConfigXml {
	
	private Context mContext = null;
	
	public ConfigXml(Context context){
		this.mContext = context;
	}
	
	//读取配置文件
	public void getElementTextValue() {
		DocumentBuilderFactory docBuilderFactory = null;
		DocumentBuilder docBuilder = null;
		Document doc = null;
		try {
			docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docBuilderFactory.newDocumentBuilder();
			// xml file 放到 assets目录中的
			doc = docBuilder.parse(mContext.getResources().getAssets().open("viewconf.xml"));
			Element root = doc.getDocumentElement();
			//System.out.println(root.getNodeName());
			NodeList items = root.getElementsByTagName("config");
			for (int i = 0; i < items.getLength(); i++) {
				// 得到第i个book节点
				Element bookNode = (Element) items.item(i);
				// 获取book节点下的所有子节点
				NodeList childsNodes = bookNode.getChildNodes();
				for (int j = 0; j < childsNodes.getLength(); j++) {
					Node node = childsNodes.item(j);
					String nodeName = node.getNodeName();
					if (Constants.SERVERINFO_VIEWNUM.equals(nodeName)) {
						Constants.viewNum = Integer.parseInt(node.getFirstChild().getNodeValue());
					} 
				}
			}
		} catch (IOException e) {
		} catch (SAXException e) {
		} catch (ParserConfigurationException e) {
		} finally {
			doc = null;
			docBuilder = null;
			docBuilderFactory = null;
		}
	}
	
	/*private byte[] InputStreamToByte(InputStream is) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        int ch;
        while ((ch = is.read()) != -1) {
            bytestream.write(ch);
        }
        byte imgdata[] = bytestream.toByteArray();
        bytestream.close();
        return imgdata;
    }*/
	
	
	@SuppressLint("NewApi")
	public void createXmlFile(String path){
        try {  
            DocumentBuilderFactory factory = DocumentBuilderFactory  
                    .newInstance();  
            DocumentBuilder builder = factory.newDocumentBuilder();  
            Document doc  = builder.newDocument();  
            //创建xml根元素  
            Element rootEle = doc.createElement("root");  
            doc.appendChild(rootEle);  
            //创建xml二级元素  
            Element nodeEle = doc.createElement("Node");  
            nodeEle.setAttribute("id", "00-00-00-00-00-00-00-00-00-00-00-00-00-00-00"); 
            nodeEle.setAttribute("num","0");
            nodeEle.setAttribute("isopen", "1");  
            nodeEle.setAttribute("status", "000");
            nodeEle.setAttribute("progressStep", "1");
            rootEle.appendChild(nodeEle);  
            TransformerFactory tf = TransformerFactory.newInstance();  
            Transformer transformer = tf.newTransformer();  
            DOMSource source = new DOMSource(doc);  
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");  
            transformer.setOutputProperty(OutputKeys.INDENT, "no");  
            StreamResult result = new StreamResult(new File(path));  
            transformer.transform(source, result);  
        } catch (ParserConfigurationException e) {  
            System.out.println(e.getMessage());  
        } catch (TransformerConfigurationException e) {  
            System.out.println(e.getMessage());  
        } catch (TransformerException e) {  
            System.out.println(e.getMessage());  
        }  
    }
	
	//更新空调配置
	public void updateElementAirConfig(String nodeId,int sensorNo,String status,int isopen,int progressStep,String path) {
		DocumentBuilderFactory docBuilderFactory = null;
		DocumentBuilder docBuilder = null;
		Document doc = null;
		try {
			path = path+"aircond.xml";
			File file = new File(path);
			InputStream is = new FileInputStream(file);
			docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docBuilderFactory.newDocumentBuilder();
			// xml file 放到 assets目录中的
			doc = docBuilder.parse(is);
			Element root = doc.getDocumentElement();
			NodeList items = root.getElementsByTagName("Node");
			List<String> list = new ArrayList<String>();
			for (int i = 0; i < items.getLength(); i++) {
				// 得到第i个book节点
				Element ele = (Element) items.item(i);
				//System.out.println(ele.getAttribute("id")+"-"+ele.getAttribute("isopen"));
				list.add(ele.getAttribute("id")+"|"+sensorNo);
				if(ele.getAttribute("id").equals(nodeId)){
					ele.setAttribute("num", sensorNo+"");
					ele.setAttribute("status", status);
					ele.setAttribute("isopen", isopen+"");
					ele.setAttribute("progressStep", progressStep+"");
				}
			}
			String te = nodeId+"|"+sensorNo;
			if(!list.contains(te)){
				Element eltName = doc.createElement("Node");
//		        Attr attr1 = doc.createAttribute("id");
//		        attr1.setValue(nodeId);
//		        Attr attr2 = doc.createAttribute("isopen");
//		        attr2.setValue(isopen+"");
//		        Attr attr3 = doc.createAttribute("status");
//		        attr3.setValue(status);
//		        Attr attr4 = doc.createAttribute("progressStep");
//		        attr4.setValue(progressStep+"");
//		        eltName.setAttributeNode(attr1);
//		        eltName.setAttributeNode(attr2);
//		        eltName.setAttributeNode(attr3);
//		        eltName.setAttributeNode(attr4);
				eltName.setAttribute("num", sensorNo+"");
				eltName.setAttribute("id", nodeId);
				eltName.setAttribute("status", status);
				eltName.setAttribute("isopen", isopen+"");
				eltName.setAttribute("progressStep", progressStep+"");
				Node stNode = doc.getElementsByTagName("root").item(0);  
				stNode.appendChild(eltName); 
			}
			doc2XmlFile(doc,path);
			//测试读到XML信息
//			InputStream is2 = new FileInputStream(file);
//			byte[] b = InputStreamToByte(is2);
//			String s = new String(b, "UTF-8");
//			System.out.println(s);
//			for (int i = 0; i < items.getLength(); i++) {
//				Element ele = (Element) items.item(i);
//				System.out.println("-------"+ele.getAttribute("id")+"-"+ele.getAttribute("isopen"));
//			}
		} catch (IOException e) {
		} catch (SAXException e) {
		} catch (ParserConfigurationException e) {
		} finally {
			doc = null;
			docBuilder = null;
			docBuilderFactory = null;
		}
	}
	
	@SuppressLint("NewApi")
	public boolean doc2XmlFile(Document document, String filename) {
        boolean flag = true;
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(filename));
            transformer.transform(source, result);
        } catch (Exception ex) {
            flag = false;
            ex.printStackTrace();
        }
        return flag;
    }
	
	
	/*private void savedXML(String fileName, String xml) {
		FileOutputStream fos = null;
		try {
			fos = mContext.openFileOutput(fileName, mContext.MODE_PRIVATE);
			byte []buffer = xml.getBytes();
			fos.write(buffer);
			fos.close();
		} catch (FileNotFoundException e) {		// mContext.openFileOutput
			e.printStackTrace();
		} catch (IOException e) {		// fos.write
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	//更新空调配置
	/*public NodeCommand_Info getElementAirConfig(String nodeId,int sensorNo,String path) {
		DocumentBuilderFactory docBuilderFactory = null;
		DocumentBuilder docBuilder = null;
		Document doc = null;
		NodeCommand_Info bean = null;
		try {
			File file = new File(path+"aircond.xml");
			InputStream is = new FileInputStream(file);
			docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docBuilderFactory.newDocumentBuilder();
			doc = docBuilder.parse(is);
			Element root = doc.getDocumentElement();
			NodeList items = root.getElementsByTagName("Node");
			for (int i = 0; i < items.getLength(); i++) {
				// 得到第i个book节点
				Element ele = (Element) items.item(i);
				//System.out.println(ele.getAttribute("id")+"-"+ele.getAttribute("isopen"));
				if(ele.getAttribute("id").equals(nodeId) && Integer.parseInt(ele.getAttribute("num")) == sensorNo){
					bean = new NodeCommand_Info();
					bean.setNodeId(ele.getAttribute("id"));
					bean.setNum(Integer.parseInt(ele.getAttribute("num")));
					bean.setIsopen(Integer.parseInt(ele.getAttribute("isopen")));
					bean.setAirStatus(ele.getAttribute("status"));
					bean.setTemperature(Integer.parseInt(ele.getAttribute("progressStep")));
				}
				
			}
			InputStream is2 = new FileInputStream(file);
			byte[] b = InputStreamToByte(is2);
			String s = new String(b, "UTF-8");
			System.out.println(s);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (SAXException e) {
			System.out.println(e.getMessage());
		} catch (ParserConfigurationException e) {
			System.out.println(e.getMessage());
		} finally {
			doc = null;
			docBuilder = null;
			docBuilderFactory = null;
		}
		return bean;
	}*/
	
	

}
