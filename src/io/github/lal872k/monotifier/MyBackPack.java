/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lal872k.monotifier;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.InteractivePage;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.logging.LogFactory;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

/**
 *
 * @author L. Arthur Lewis II
 */
public class MyBackPack {
    
    public static MO[] loadMOs(User user, Engine engine) throws IOException{
        System.out.println("Retrieving MO's for user "+user.getID()+" ("+user.getName()+").");
        // make web client
        final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_45);
        
        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF); 
        java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

        //webClient.setCssEnabled(false);
        // http://stackoverflow.com/questions/3600557/turning-htmlunit-warnings-off
        
        webClient.setIncorrectnessListener(new IncorrectnessListener() {

            @Override
            public void notify(String arg0, Object arg1) {
                // TODO Auto-generated method stub

            }
        });
        webClient.setCssErrorHandler(new ErrorHandler() {

            @Override
            public void warning(CSSParseException exception) throws CSSException {
                // TODO Auto-generated method stub

            }

            @Override
            public void fatalError(CSSParseException exception) throws CSSException {
                // TODO Auto-generated method stub

            }

            @Override
            public void error(CSSParseException exception) throws CSSException {
                // TODO Auto-generated method stub

            }
        });
        webClient.setJavaScriptErrorListener(new JavaScriptErrorListener() {


            @Override
            public void scriptException(InteractivePage ip, ScriptException se) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void timeoutError(InteractivePage ip, long l, long l1) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void malformedScriptURL(InteractivePage ip, String string, MalformedURLException murle) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void loadScriptError(InteractivePage ip, URL url, Exception excptn) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        webClient.setHTMLParserListener(new HTMLParserListener() {


            @Override
            public void error(String string, URL url, String string1, int i, int i1, String string2) {
                //
            }

            @Override
            public void warning(String string, URL url, String string1, int i, int i1, String string2) {
                //
            }
        });

        //webClient.setThrowExceptionOnFailingStatusCode(false);
        //webClient.setThrowExceptionOnScriptError(false);
        
        //http://stackoverflow.com/questions/19551043/process-ajax-request-in-htmlunit/26268815#26268815
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        
        // get the page
        HtmlPage page = webClient.getPage("https://peddie.seniormbp.com/SeniorApps/studentParent/attendSummary.faces?selectedMenuId=true");
        
        // get login form
        final HtmlForm login_form = page.getFormByName("form");
        
        final HtmlTextInput username = login_form.getInputByName("form:userId");
        final HtmlPasswordInput password = login_form.getInputByName("form:userPassword");
        
        final HtmlButtonInput login_submit = login_form.getInputByName("form:signIn");
        
        username.setValueAttribute(user.getMyBackPackUsername());
        password.setValueAttribute(user.getDecryptedMyBackPackPassword());
        
        URL oldURL = page.getUrl();
        
        // Now submit the form by clicking the button and get back the new page.
        page = login_submit.click();
        
        if (oldURL.equals(page.getUrl())){
            System.err.println("Password or username was invalid for " + user.getName()+"("+user.getID()+").");
            return new MO[0];
        }
        
        // click on details
        
        final HtmlForm switchView_form = page.getFormByName("j_id_jsp_1447653194_2");
        
        final HtmlSubmitInput switchView_submit = switchView_form.getInputByName("j_id_jsp_1447653194_2:j_id_jsp_1447653194_12");
        
        page = switchView_submit.click();
        
        // now on right page
        
        ArrayList<MO> mos = new ArrayList();
        
        // find all rows
        List<DomElement> odds = (List<DomElement>) page.getByXPath("//tr[@class='dataCellOdd' or @class='dataCellEven']");
        
        String date = "";
        String section = "";
        boolean passedSection = false;
        
        for (DomElement el : odds){
            for (DomElement ele : el.getChildElements()){
                // date is only on with rowspan
                if (ele.hasAttribute("rowspan")){
                    date = ele.getTextContent();
                }
                // section and type contain that attr
                if (ele.getAttribute("class").equals("attendTypeColumnData2")){
                    // section comes first
                    if (!passedSection){
                        section = ele.getTextContent();
                        passedSection = true;
                    } else {
                        mos.add(new MO(section, date, ele.getTextContent()));
                        passedSection = false;
                    }
                }
            }
        }
        
        engine.getHistory().addAction(new Action("Accessed MyBackPack", "Accessed the MyBackPack of " + user.getName() + " (" + user.getID() + ")("+user.getEmail()+") to update the mo count."));
        
        return mos.toArray(new MO[0]);
        
    }
    
    public static boolean hasValidCredentials(User user, Engine engine) throws IOException{
        System.out.println("Checking validity of " + user.getName() + "'s credentials for MyBackPack...");
        
        final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_45);
        
        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF); 
        java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

        //webClient.setCssEnabled(false);
        // http://stackoverflow.com/questions/3600557/turning-htmlunit-warnings-off
        
        webClient.setIncorrectnessListener(new IncorrectnessListener() {

            @Override
            public void notify(String arg0, Object arg1) {
                // TODO Auto-generated method stub

            }
        });
        webClient.setCssErrorHandler(new ErrorHandler() {

            @Override
            public void warning(CSSParseException exception) throws CSSException {
                // TODO Auto-generated method stub

            }

            @Override
            public void fatalError(CSSParseException exception) throws CSSException {
                // TODO Auto-generated method stub

            }

            @Override
            public void error(CSSParseException exception) throws CSSException {
                // TODO Auto-generated method stub

            }
        });
        webClient.setJavaScriptErrorListener(new JavaScriptErrorListener() {


            @Override
            public void scriptException(InteractivePage ip, ScriptException se) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void timeoutError(InteractivePage ip, long l, long l1) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void malformedScriptURL(InteractivePage ip, String string, MalformedURLException murle) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void loadScriptError(InteractivePage ip, URL url, Exception excptn) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        webClient.setHTMLParserListener(new HTMLParserListener() {


            @Override
            public void error(String string, URL url, String string1, int i, int i1, String string2) {
                //
            }

            @Override
            public void warning(String string, URL url, String string1, int i, int i1, String string2) {
                //
            }
        });

        //webClient.setThrowExceptionOnFailingStatusCode(false);
        //webClient.setThrowExceptionOnScriptError(false);
        
        //http://stackoverflow.com/questions/19551043/process-ajax-request-in-htmlunit/26268815#26268815
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        
        // get the page
        HtmlPage page = webClient.getPage("https://peddie.seniormbp.com/SeniorApps/studentParent/attendSummary.faces?selectedMenuId=true");
        
        // get login form
        final HtmlForm login_form = page.getFormByName("form");
        
        final HtmlTextInput username = login_form.getInputByName("form:userId");
        final HtmlPasswordInput password = login_form.getInputByName("form:userPassword");
        
        final HtmlButtonInput login_submit = login_form.getInputByName("form:signIn");
        
        username.setValueAttribute(user.getMyBackPackUsername());
        password.setValueAttribute(user.getDecryptedMyBackPackPassword());
        
        URL oldURL = page.getUrl();
        
        // Now submit the form by clicking the button and get back the new page.
        page = login_submit.click();
        
        engine.getHistory().addAction(new Action("Accessed MyBackPack", "Accessed the MyBackPack of " + user.getName() + " (" + user.getID() + ")("+user.getEmail()+") to check the validity of the account credentials."));
        
        if (oldURL.equals(page.getUrl())){
            return false;
        }
        
        return true;
    }
    
}
