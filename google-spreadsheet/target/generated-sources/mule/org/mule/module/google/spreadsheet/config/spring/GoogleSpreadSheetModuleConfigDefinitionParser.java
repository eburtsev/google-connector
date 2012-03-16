
package org.mule.module.google.spreadsheet.config.spring;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.mule.api.lifecycle.Disposable;
import org.mule.api.lifecycle.Initialisable;
import org.mule.config.spring.MuleHierarchicalBeanDefinitionParserDelegate;
import org.mule.config.spring.parsers.generic.AutoIdUtils;
import org.mule.config.spring.util.SpringXMLUtils;
import org.mule.module.google.spreadsheet.config.GoogleSpreadSheetModuleOAuth1Adapter;
import org.mule.module.google.spreadsheet.config.RestoreAccessTokenCallbackFactoryBean;
import org.mule.module.google.spreadsheet.config.SaveAccessTokenCallbackFactoryBean;
import org.mule.util.TemplateParser;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class GoogleSpreadSheetModuleConfigDefinitionParser
    implements BeanDefinitionParser
{

    /**
     * Mule Pattern Info
     * 
     */
    private TemplateParser.PatternInfo patternInfo;

    public GoogleSpreadSheetModuleConfigDefinitionParser() {
        patternInfo = TemplateParser.createMuleStyleParser().getStyle();
    }

    public BeanDefinition parse(Element element, ParserContext parserContent) {
        String name = element.getAttribute("name");
        if ((name == null)||StringUtils.isBlank(name)) {
            element.setAttribute("name", AutoIdUtils.getUniqueName(element, "mule-bean"));
        }
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(GoogleSpreadSheetModuleOAuth1Adapter.class.getName());
        if (Initialisable.class.isAssignableFrom(GoogleSpreadSheetModuleOAuth1Adapter.class)) {
            builder.setInitMethodName(Initialisable.PHASE_NAME);
        }
        if (Disposable.class.isAssignableFrom(GoogleSpreadSheetModuleOAuth1Adapter.class)) {
            builder.setDestroyMethodName(Disposable.PHASE_NAME);
        }
        if ((element.getAttribute("consumerKey")!= null)&&(!StringUtils.isBlank(element.getAttribute("consumerKey")))) {
            builder.addPropertyValue("consumerKey", element.getAttribute("consumerKey"));
        }
        if ((element.getAttribute("consumerSecret")!= null)&&(!StringUtils.isBlank(element.getAttribute("consumerSecret")))) {
            builder.addPropertyValue("consumerSecret", element.getAttribute("consumerSecret"));
        }
        if ((element.getAttribute("scope")!= null)&&(!StringUtils.isBlank(element.getAttribute("scope")))) {
            builder.addPropertyValue("scope", element.getAttribute("scope"));
        }
        Element httpCallbackConfigElement = DomUtils.getChildElementByTagName(element, "oauth-callback-config");
        if (httpCallbackConfigElement!= null) {
            if ((httpCallbackConfigElement.getAttribute("domain")!= null)&&(!StringUtils.isBlank(httpCallbackConfigElement.getAttribute("domain")))) {
                builder.addPropertyValue("domain", httpCallbackConfigElement.getAttribute("domain"));
            }
            if ((httpCallbackConfigElement.getAttribute("localPort")!= null)&&(!StringUtils.isBlank(httpCallbackConfigElement.getAttribute("localPort")))) {
                builder.addPropertyValue("localPort", httpCallbackConfigElement.getAttribute("localPort"));
            }
            if ((httpCallbackConfigElement.getAttribute("remotePort")!= null)&&(!StringUtils.isBlank(httpCallbackConfigElement.getAttribute("remotePort")))) {
                builder.addPropertyValue("remotePort", httpCallbackConfigElement.getAttribute("remotePort"));
            }
            if ((httpCallbackConfigElement.getAttribute("async")!= null)&&(!StringUtils.isBlank(httpCallbackConfigElement.getAttribute("async")))) {
                builder.addPropertyValue("async", httpCallbackConfigElement.getAttribute("async"));
            }
            if ((httpCallbackConfigElement.getAttribute("connector-ref")!= null)&&(!StringUtils.isBlank(httpCallbackConfigElement.getAttribute("connector-ref")))) {
                builder.addPropertyValue("connector", new RuntimeBeanReference(httpCallbackConfigElement.getAttribute("connector-ref")));
            }
        }
        Element oauthSaveAccessTokenElement = DomUtils.getChildElementByTagName(element, "oauth-save-access-token");
        if (oauthSaveAccessTokenElement!= null) {
            BeanDefinitionBuilder oauthSaveAccessTokenBeanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(SaveAccessTokenCallbackFactoryBean.class);
            BeanDefinition oauthSaveAccessTokenBeanDefinition = oauthSaveAccessTokenBeanDefinitionBuilder.getBeanDefinition();
            parserContent.getRegistry().registerBeanDefinition(generateChildBeanName(oauthSaveAccessTokenElement), oauthSaveAccessTokenBeanDefinition);
            oauthSaveAccessTokenElement.setAttribute("name", generateChildBeanName(oauthSaveAccessTokenElement));
            oauthSaveAccessTokenBeanDefinitionBuilder.setSource(parserContent.extractSource(oauthSaveAccessTokenElement));
            oauthSaveAccessTokenBeanDefinitionBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
            List oauthSaveAccessTokenList = parserContent.getDelegate().parseListElement(oauthSaveAccessTokenElement, oauthSaveAccessTokenBeanDefinitionBuilder.getBeanDefinition());
            parserContent.getRegistry().removeBeanDefinition(generateChildBeanName(oauthSaveAccessTokenElement));
            builder.addPropertyValue("oauthSaveAccessToken", oauthSaveAccessTokenBeanDefinition);
        }
        Element oauthRestoreAccessTokenElement = DomUtils.getChildElementByTagName(element, "oauth-restore-access-token");
        if (oauthRestoreAccessTokenElement!= null) {
            BeanDefinitionBuilder oauthRestoreAccessTokenBeanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(RestoreAccessTokenCallbackFactoryBean.class);
            BeanDefinition oauthRestoreAccessTokenBeanDefinition = oauthRestoreAccessTokenBeanDefinitionBuilder.getBeanDefinition();
            parserContent.getRegistry().registerBeanDefinition(generateChildBeanName(oauthRestoreAccessTokenElement), oauthRestoreAccessTokenBeanDefinition);
            oauthRestoreAccessTokenElement.setAttribute("name", generateChildBeanName(oauthRestoreAccessTokenElement));
            oauthRestoreAccessTokenBeanDefinitionBuilder.setSource(parserContent.extractSource(oauthRestoreAccessTokenElement));
            oauthRestoreAccessTokenBeanDefinitionBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
            List oauthRestoreAccessTokenList = parserContent.getDelegate().parseListElement(oauthRestoreAccessTokenElement, oauthRestoreAccessTokenBeanDefinitionBuilder.getBeanDefinition());
            parserContent.getRegistry().removeBeanDefinition(generateChildBeanName(oauthRestoreAccessTokenElement));
            builder.addPropertyValue("oauthRestoreAccessToken", oauthRestoreAccessTokenBeanDefinition);
        }
        BeanDefinition definition = builder.getBeanDefinition();
        definition.setAttribute(MuleHierarchicalBeanDefinitionParserDelegate.MULE_NO_RECURSE, Boolean.TRUE);
        return definition;
    }

    private String generateChildBeanName(Element element) {
        String id = SpringXMLUtils.getNameOrId(element);
        if (StringUtils.isBlank(id)) {
            String parentId = SpringXMLUtils.getNameOrId(((Element) element.getParentNode()));
            return ((("."+ parentId)+":")+ element.getLocalName());
        } else {
            return id;
        }
    }

}
