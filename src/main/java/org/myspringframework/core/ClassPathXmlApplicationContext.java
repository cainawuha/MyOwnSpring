package org.myspringframework.core;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassPathXmlApplicationContext implements ApplicationContext{

    private static final Logger logger = LoggerFactory.getLogger(ClassPathXmlApplicationContext.class);
    private Map<String,Object> singletonObjects  =new HashMap<>();

    public ClassPathXmlApplicationContext(String configLocation) {
        try{
            //resolve myspring.xml
            //new bean
            //singletonObjects.put(bean)

            SAXReader reader = new SAXReader();
            InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(configLocation);
            Document document = reader.read(in);
            List<Node> nodes = document.selectNodes("//bean");

            nodes.forEach(node -> {
                try{
                    // System.out.println(node);
                    Element beanElt = (Element) node;
                    String id = beanElt.attributeValue("id");
                    String className = beanElt.attributeValue("class");
                    logger.info("bean name = " +id);
                    logger.info("beanClassName = "+className);

                    Class<?> clazz = Class.forName(className);
                    Constructor<?> defaultCon = clazz.getDeclaredConstructor();
                    Object bean = defaultCon.newInstance();

                    singletonObjects.put(id,bean);
                    logger.info(singletonObjects.toString());

                }catch(Exception e){
                    e.printStackTrace();
                }
            });

            nodes.forEach(node -> {
                try{
                    Element beanElt = (Element) node;
                    String id = beanElt.attributeValue("id");
                    String className = beanElt.attributeValue("class");
                    Class<?>  aClass =Class.forName(className);
                    List<Element> properties = beanElt.elements("property");
                    properties.forEach(property ->{

                        String propertyName = property.attributeValue("name");
                        try {
                            Field field = aClass.getDeclaredField(propertyName);
                            logger.info("property name is "+propertyName);

                            String setMethodName = "set"+propertyName.toUpperCase().charAt(0)+propertyName.substring(1);
                            Method setMethod = aClass.getDeclaredMethod(setMethodName,field.getType());

                            String value = property.attributeValue("value");//"30"
                            String ref = property.attributeValue("ref");
                            Object actualValue = null;
                            if(value!=null){
                                // byte short int long float double boolean char
                                // Byte Short Integer Long Float Double Boolean Character String
                                String propertyTypeName = field.getType().getSimpleName();//"String"
                                switch (propertyTypeName){
                                    case "byte":
                                         actualValue = Byte.parseByte(value);
                                        break;
                                    case "short":
                                        actualValue = Short.parseShort(value);
                                        break;

                                    case "int":
                                        actualValue = Integer.parseInt(value);
                                        break;
                                    case "long":
                                        actualValue = Long.parseLong(value);
                                        break;
                                    case "float":
                                        actualValue = Float.parseFloat(value);
                                        break;
                                    case "double":
                                        actualValue =Double.parseDouble(value);
                                        break;
                                    case "boolean":
                                        actualValue =Boolean.parseBoolean(value);
                                        break;
                                    case "char":
                                        actualValue = value.charAt(0);
                                        break;
                                    case "Byte":
                                        actualValue =Byte.valueOf(value);
                                        break;
                                    case "Short":
                                        actualValue =Short.valueOf(value);
                                        break;
                                    case "Integer":
                                        actualValue = Integer.valueOf(value);
                                        break;
                                    case "Long":
                                        actualValue =Long.valueOf(value);
                                        break;
                                    case "Float":
                                        actualValue =Float.valueOf(value);
                                        break;
                                    case "Double":
                                        actualValue =Double.valueOf(value);
                                        break;
                                    case "Boolean":
                                        actualValue =Boolean.valueOf(value);
                                        break;
                                    case "Character":
                                        actualValue =Character.valueOf(value.charAt(0));
                                        break;
                                    case "String":
                                        actualValue=value;

                                }
                                setMethod.invoke(singletonObjects.get(id),actualValue);
                            }
                            if(ref !=null){
                                setMethod.invoke(singletonObjects.get(id),singletonObjects.get(ref));
                            }



                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }catch(Exception e ){
                    e.printStackTrace();
                }

            });


        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public Object getBean(String beanName) {
        return singletonObjects.get(beanName);
    }
}
