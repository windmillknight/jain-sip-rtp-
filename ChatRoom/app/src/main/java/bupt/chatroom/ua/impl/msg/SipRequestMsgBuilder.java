package bupt.chatroom.ua.impl.msg;

import android.javax.sip.InvalidArgumentException;
import android.javax.sip.SipProvider;
import android.javax.sip.address.Address;
import android.javax.sip.address.AddressFactory;
import android.javax.sip.address.SipURI;
import android.javax.sip.address.URI;
import android.javax.sip.header.CSeqHeader;
import android.javax.sip.header.CallIdHeader;
import android.javax.sip.header.ContactHeader;
import android.javax.sip.header.ContentTypeHeader;
import android.javax.sip.header.ExpiresHeader;
import android.javax.sip.header.FromHeader;
import android.javax.sip.header.Header;
import android.javax.sip.header.HeaderFactory;
import android.javax.sip.header.MaxForwardsHeader;
import android.javax.sip.header.RouteHeader;
import android.javax.sip.header.ToHeader;
import android.javax.sip.header.ViaHeader;
import android.javax.sip.message.MessageFactory;
import android.javax.sip.message.Request;

import bupt.chatroom.ua.impl.DeviceImpl;
import bupt.chatroom.ua.impl.SipManager;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SipRequestMsgBuilder {

    public Request buildRegister(SipManager sipManager,String register) throws ParseException, InvalidArgumentException {
        AddressFactory addressFactory = sipManager.addressFactory;
        SipProvider sipProvider = sipManager.sipProvider;
        MessageFactory messageFactory = sipManager.messageFactory;
        HeaderFactory headerFactory = sipManager.headerFactory;

        // Create addresses and via header for the request
        Address fromAddress = addressFactory.createAddress("sip:"
                + DeviceImpl.getInstance().getSipProfile().getSipUserName() + "@"
                + DeviceImpl.getInstance().getSipProfile().getLocalIp());
        fromAddress.setDisplayName(DeviceImpl.getInstance().getSipProfile().getSipUserName());
        Address toAddress = addressFactory.createAddress("sip:"
                + DeviceImpl.getInstance().getSipProfile().getSipUserName() + "@"
                + DeviceImpl.getInstance().getSipProfile().getRemoteIp());
        toAddress.setDisplayName("server");
        Address contactAddress = sipManager.createContactAddress();
        ArrayList<ViaHeader> viaHeaders = sipManager.createViaHeader();
        URI requestURI = addressFactory.createAddress(
                "sip:" + DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint()).getURI();

        // Build the request
        final Request request = messageFactory.createRequest(requestURI,
                Request.REGISTER, sipProvider.getNewCallId(),
                headerFactory.createCSeqHeader(1l, Request.REGISTER),
                headerFactory.createFromHeader(fromAddress, "c3ff411e"),
                headerFactory.createToHeader(toAddress, null), viaHeaders,
                headerFactory.createMaxForwardsHeader(70));

        // Add the contact header
        request.addHeader(headerFactory.createContactHeader(contactAddress));
        ExpiresHeader eh = headerFactory.createExpiresHeader(300);
        request.addHeader(eh);

        ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text","plain");
        request.setContent(register,contentTypeHeader);
        // Print the request
        System.out.println(request.toString());

        return request;

        // Send the request --- triggers an IOException
        // sipProvider.sendRequest(request);
        // ClientTransaction transaction = sipProvider.getNewClientTransaction(request);
        // Send the request statefully, through the client transaction.
        // transaction.sendRequest();
    }

    public Request buildRegister(SipManager sipManager) throws ParseException, InvalidArgumentException {
        AddressFactory addressFactory = sipManager.addressFactory;
        SipProvider sipProvider = sipManager.sipProvider;
        MessageFactory messageFactory = sipManager.messageFactory;
        HeaderFactory headerFactory = sipManager.headerFactory;

        // Create addresses and via header for the request
        Address fromAddress = addressFactory.createAddress("sip:"
                + DeviceImpl.getInstance().getSipProfile().getSipUserName() + "@"
                + DeviceImpl.getInstance().getSipProfile().getLocalIp());
        fromAddress.setDisplayName(DeviceImpl.getInstance().getSipProfile().getSipUserName());
        Address toAddress = addressFactory.createAddress("sip:"
                + DeviceImpl.getInstance().getSipProfile().getSipUserName() + "@"
                + DeviceImpl.getInstance().getSipProfile().getRemoteIp());
        toAddress.setDisplayName("server");
        Address contactAddress = sipManager.createContactAddress();
        ArrayList<ViaHeader> viaHeaders = sipManager.createViaHeader();
        URI requestURI = addressFactory.createAddress(
                "sip:" + DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint()).getURI();

        // Build the request
        final Request request = messageFactory.createRequest(requestURI,
                Request.REGISTER, sipProvider.getNewCallId(),
                headerFactory.createCSeqHeader(1l, Request.REGISTER),
                headerFactory.createFromHeader(fromAddress, "c3ff411e"),
                headerFactory.createToHeader(toAddress, null), viaHeaders,
                headerFactory.createMaxForwardsHeader(70));

        // Add the contact header
        request.addHeader(headerFactory.createContactHeader(contactAddress));
        ExpiresHeader eh = headerFactory.createExpiresHeader(300);
        request.addHeader(eh);
        // Print the request
        System.out.println(request.toString());

        return request;

        // Send the request --- triggers an IOException
        // sipProvider.sendRequest(request);
        // ClientTransaction transaction = sipProvider.getNewClientTransaction(request);
        // Send the request statefully, through the client transaction.
        // transaction.sendRequest();
    }

    public Request buildMessage(SipManager sipManager, String targetName, String to, String message) throws ParseException, InvalidArgumentException {

        /*from header*/
        SipURI from = sipManager.addressFactory.createSipURI(DeviceImpl.getInstance().getSipProfile().getSipUserName(), DeviceImpl.getInstance().getSipProfile().getLocalEndpoint());
        Address fromNameAddress = sipManager.addressFactory.createAddress(from);
        fromNameAddress.setDisplayName(DeviceImpl.getInstance().getSipProfile().getSipUserName());
        FromHeader fromHeader = sipManager.headerFactory.createFromHeader(fromNameAddress,
                "groupChatv1.0");
        //"Tzt0ZEP92");

        /*to header 私聊对象的地址*/
        URI toAddress = sipManager.addressFactory.createSipURI(targetName, to);
        Address toNameAddress = sipManager.addressFactory.createAddress(toAddress);
        toNameAddress.setDisplayName(targetName);
        ToHeader toHeader = sipManager.headerFactory.createToHeader(toNameAddress, null);

        SipURI requestURI = sipManager.addressFactory.createSipURI("server",
                DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint());

        //URI requestURI = sipManager.addressFactory.createURI(to);
        // requestURI.setTransportParam("udp");

        ArrayList<ViaHeader> viaHeaders = sipManager.createViaHeader();

        CallIdHeader callIdHeader = sipManager.sipProvider.getNewCallId();

        CSeqHeader cSeqHeader = sipManager.headerFactory.createCSeqHeader(50l,
                Request.MESSAGE);

        MaxForwardsHeader maxForwards = sipManager.headerFactory
                .createMaxForwardsHeader(70);

        Request request = sipManager.messageFactory.createRequest(requestURI,
                Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
                toHeader, viaHeaders, maxForwards);


 /*       SipURI routeUri = sipManager.addressFactory.createSipURI(null, DeviceImpl.getInstance().getSipProfile().getRemoteIp());
        routeUri.setTransportParam(DeviceImpl.getInstance().getSipProfile().getTransport());
        routeUri.setLrParam();
        routeUri.setPort(DeviceImpl.getInstance().getSipProfile().getRemotePort());

        Address routeAddress = sipManager.addressFactory.createAddress(routeUri);
        RouteHeader route = sipManager.headerFactory.createRouteHeader(routeAddress);
        request.addHeader(route);*/
        ContentTypeHeader contentTypeHeader = sipManager.headerFactory
                .createContentTypeHeader("text", "message");
        request.setContent(message, contentTypeHeader);
        return request;
        //ClientTransaction transaction = sipManager.sipProvider
        //		.getNewClientTransaction(request);
        // Send the request statefully, through the client transaction.
        //transaction.sendRequest();

        /*SipURI from = sipManager.addressFactory.createSipURI(DeviceImpl.getInstance().getSipProfile().getSipUserName(), DeviceImpl.getInstance().getSipProfile().getLocalEndpoint());
        Address fromNameAddress = sipManager.addressFactory.createAddress(from);
        fromNameAddress.setDisplayName(DeviceImpl.getInstance().getSipProfile().getSipUserName());
        FromHeader fromHeader = sipManager.headerFactory.createFromHeader(fromNameAddress,
                "textclientv1.0");

        String username = to.substring(to.indexOf(":") + 1, to.indexOf("@"));
        String address = to.substring(to.indexOf("@") + 1);

        SipURI toAddress = sipManager.addressFactory.createSipURI(username, address);
        Address toNameAddress = sipManager.addressFactory.createAddress(toAddress);
        toNameAddress.setDisplayName(username);
        ToHeader toHeader = sipManager.headerFactory.createToHeader(toNameAddress, null);

        SipURI requestURI = sipManager.addressFactory.createSipURI(username, address);
        requestURI.setTransportParam("udp");

        ArrayList viaHeaders = new ArrayList();
        ViaHeader viaHeader = sipManager.headerFactory.createViaHeader(DeviceImpl.getInstance().getSipProfile().getLocalIp(),
                DeviceImpl.getInstance().getSipProfile().getLocalPort(), "udp", "branch1");
        viaHeaders.add(viaHeader);

        CallIdHeader callIdHeader = sipManager.sipProvider.getNewCallId();

        CSeqHeader cSeqHeader = sipManager.headerFactory.createCSeqHeader(1,
                Request.MESSAGE);

        MaxForwardsHeader maxForwards = sipManager.headerFactory
                .createMaxForwardsHeader(70);

        Request request = sipManager.messageFactory.createRequest(requestURI,
                Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
                toHeader, viaHeaders, maxForwards);

        SipURI contactURI = sipManager.addressFactory.createSipURI(DeviceImpl.getInstance().getSipProfile().getSipUserName(),
                DeviceImpl.getInstance().getSipProfile().getLocalIp());
        contactURI.setPort(DeviceImpl.getInstance().getSipProfile().getLocalPort());
        Address contactAddress = sipManager.addressFactory.createAddress(contactURI);
        contactAddress.setDisplayName(DeviceImpl.getInstance().getSipProfile().getSipUserName());
        ContactHeader contactHeader = sipManager.headerFactory
                .createContactHeader(contactAddress);
        request.addHeader(contactHeader);

        ContentTypeHeader contentTypeHeader = sipManager.headerFactory
                .createContentTypeHeader("text", "plain");
        request.setContent(message, contentTypeHeader);
        return request;*/
    }

    public Request buildInvite(SipManager sipManager, String to) {

        try {
            /*create from header*/
            SipURI from = sipManager.addressFactory.createSipURI(DeviceImpl.getInstance().getSipProfile().getSipUserName(),
                    DeviceImpl.getInstance().getSipProfile().getLocalEndpoint());
            Address fromNameAddress = sipManager.addressFactory.createAddress(from);
            fromNameAddress.setDisplayName(DeviceImpl.getInstance().getSipProfile().getSipUserName());
            FromHeader fromHeader = sipManager.headerFactory.createFromHeader(fromNameAddress,
                    "Tzt0ZEP92");

            /*to header 此处填服务器地址*/
            URI toAddress = sipManager.addressFactory.createSipURI("server", DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint());
            Address toNameAddress = sipManager.addressFactory.createAddress(toAddress);
            toNameAddress.setDisplayName("server");
            ToHeader toHeader = sipManager.headerFactory.createToHeader(toNameAddress, null);

            /*request URI 还是写服务器*/
            // URI requestURI = sipManager.addressFactory.createURI(DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint());
            // requestURI.setTransportParam("udp");
            // create Request URI
            SipURI requestURI = sipManager.addressFactory.createSipURI("server",
                    DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint());

            /*via headers*/
            ArrayList<ViaHeader> viaHeaders = sipManager.createViaHeader();

            /*callId header*/
            CallIdHeader callIdHeader = sipManager.sipProvider.getNewCallId();

            /*Cseq header*/
            CSeqHeader cSeqHeader = sipManager.headerFactory.createCSeqHeader(1l,
                    Request.INVITE);

            /*max forwards header*/
            MaxForwardsHeader maxForwards = sipManager.headerFactory
                    .createMaxForwardsHeader(70);
            /*create the request*/
            Request callRequest = sipManager.messageFactory.createRequest(requestURI,
                    Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);
            /*
            extra headers, optional
            SupportedHeader supportedHeader = sipManager.headerFactory
                    .createSupportedHeader("replaces, outbound");
            callRequest.addHeader(supportedHeader);*/
            addCustomHeaders(callRequest, sipManager);

            SipURI routeUri = sipManager.addressFactory.createSipURI(null, DeviceImpl.getInstance().getSipProfile().getRemoteIp());
            routeUri.setTransportParam(DeviceImpl.getInstance().getSipProfile().getTransport());
            routeUri.setLrParam();
            routeUri.setPort(DeviceImpl.getInstance().getSipProfile().getRemotePort());

            Address routeAddress = sipManager.addressFactory.createAddress(routeUri);
            RouteHeader route = sipManager.headerFactory.createRouteHeader(routeAddress);
            callRequest.addHeader(route);
           /* // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = sipManager.headerFactory
                    .createContentTypeHeader("application", "sdp");
*/
            // Create the contact name address.
            SipURI contactURI = sipManager.addressFactory.createSipURI(DeviceImpl.getInstance().getSipProfile().getSipUserName(), DeviceImpl.getInstance().getSipProfile().getLocalIp());
            contactURI.setPort(sipManager.sipProvider.getListeningPoint(DeviceImpl.getInstance().getSipProfile().getTransport())
                    .getPort());

            Address contactAddress = sipManager.addressFactory.createAddress(contactURI);

            // Add the contact address.
            //contactAddress.setDisplayName(fromName);

            ContactHeader contactHeader = sipManager.headerFactory.createContactHeader(contactAddress);
            callRequest.addHeader(contactHeader);

            // You can add extension headers of your own making
            // to the outgoing SIP request.
          /*  // Add the extension header.
            Header extensionHeader = sipManager.headerFactory.createHeader("My-Header",
                    "my header value");
            callRequest.addHeader(extensionHeader);*/
         /*   String sdpData = "";

            byte[] contents = sdpData.getBytes();
*/
           /* callRequest.setContent(contents, contentTypeHeader);*/
            // You can add as many extension headers as you
            // want.

            /*extensionHeader = sipManager.headerFactory.createHeader("My-Other-Header",
                    "my new header value ");
            callRequest.addHeader(extensionHeader);*/
/*
            Header callInfoHeader = sipManager.headerFactory.createHeader("Call-Info",
                    "<http://www.antd.nist.gov>");
            callRequest.addHeader(callInfoHeader);*/
            return callRequest;

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();

        }
        return null;
    }

    public Request buildAction(SipManager sipManager, String targetUser,String action) {

        try {
            String items[]=targetUser.substring(4).split("@");
            String targetName=items[0];
            String targetAddress=items[1];
            /*create from header*/
            SipURI from = sipManager.addressFactory.createSipURI(DeviceImpl.getInstance().getSipProfile().getSipUserName(),
                    DeviceImpl.getInstance().getSipProfile().getLocalEndpoint());
            Address fromNameAddress = sipManager.addressFactory.createAddress(from);
            fromNameAddress.setDisplayName(DeviceImpl.getInstance().getSipProfile().getSipUserName());
            FromHeader fromHeader = sipManager.headerFactory.createFromHeader(fromNameAddress,
                    "Tzt0ZEP92");

            /*to header 此处填所请求的对象*/
            URI toAddress = sipManager.addressFactory.createSipURI(targetName, targetAddress);
            Address toNameAddress = sipManager.addressFactory.createAddress(toAddress);
            toNameAddress.setDisplayName(targetName);
            ToHeader toHeader = sipManager.headerFactory.createToHeader(toNameAddress, null);

            /*request URI 还是写服务器*/
            // URI requestURI = sipManager.addressFactory.createURI(DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint());
            // requestURI.setTransportParam("udp");
            // create Request URI
            SipURI requestURI = sipManager.addressFactory.createSipURI(targetName,
                    DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint());

            /*via headers*/
            ArrayList<ViaHeader> viaHeaders = sipManager.createViaHeader();

            /*callId header*/
            CallIdHeader callIdHeader = sipManager.sipProvider.getNewCallId();

            /*Cseq header*/
            CSeqHeader cSeqHeader = sipManager.headerFactory.createCSeqHeader(1l,
                    Request.MESSAGE);

            /*max forwards header*/
            MaxForwardsHeader maxForwards = sipManager.headerFactory
                    .createMaxForwardsHeader(70);
            /*create the request*/
            Request callRequest = sipManager.messageFactory.createRequest(requestURI,
                    Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);

            addCustomHeaders(callRequest, sipManager);

            SipURI routeUri = sipManager.addressFactory.createSipURI(null, DeviceImpl.getInstance().getSipProfile().getRemoteIp());
            routeUri.setTransportParam(DeviceImpl.getInstance().getSipProfile().getTransport());
            routeUri.setLrParam();
            routeUri.setPort(DeviceImpl.getInstance().getSipProfile().getRemotePort());

            Address routeAddress = sipManager.addressFactory.createAddress(routeUri);
            RouteHeader route = sipManager.headerFactory.createRouteHeader(routeAddress);
            callRequest.addHeader(route);


            // Create the contact name address.
            SipURI contactURI = sipManager.addressFactory.createSipURI(DeviceImpl.getInstance().getSipProfile().getSipUserName(), DeviceImpl.getInstance().getSipProfile().getLocalIp());
            contactURI.setPort(sipManager.sipProvider.getListeningPoint(DeviceImpl.getInstance().getSipProfile().getTransport())
                    .getPort());

            Address contactAddress = sipManager.addressFactory.createAddress(contactURI);

            // Add the contact address.
            //contactAddress.setDisplayName(fromName);

            ContactHeader contactHeader = sipManager.headerFactory.createContactHeader(contactAddress);

            // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = sipManager.headerFactory
                    .createContentTypeHeader("action", action);
            callRequest.setContent(targetName, contentTypeHeader);// the action sends with a blank body

            callRequest.addHeader(contactHeader);

            return callRequest;

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();

        }
        return null;
    }

    public Request buildBye(SipManager sipManager) {

        try {
            /*create from header*/
            SipURI from = sipManager.addressFactory.createSipURI(DeviceImpl.getInstance().getSipProfile().getSipUserName(),
                    DeviceImpl.getInstance().getSipProfile().getLocalEndpoint());
            Address fromNameAddress = sipManager.addressFactory.createAddress(from);
            fromNameAddress.setDisplayName(DeviceImpl.getInstance().getSipProfile().getSipUserName());
            FromHeader fromHeader = sipManager.headerFactory.createFromHeader(fromNameAddress,
                    "Tzt0ZEP92");

            /*to header 此处填服务器地址*/
            URI toAddress = sipManager.addressFactory.createSipURI("server", DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint());
            Address toNameAddress = sipManager.addressFactory.createAddress(toAddress);
            toNameAddress.setDisplayName("server");
            ToHeader toHeader = sipManager.headerFactory.createToHeader(toNameAddress, null);

            /*request URI 还是写服务器*/
            // URI requestURI = sipManager.addressFactory.createURI(DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint());
            // requestURI.setTransportParam("udp");
            // create Request URI
            SipURI requestURI = sipManager.addressFactory.createSipURI("server",
                    DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint());

            /*via headers*/
            ArrayList<ViaHeader> viaHeaders = sipManager.createViaHeader();

            /*callId header*/
            CallIdHeader callIdHeader = sipManager.sipProvider.getNewCallId();

            /*Cseq header*/
            CSeqHeader cSeqHeader = sipManager.headerFactory.createCSeqHeader(1l,
                    Request.BYE);

            /*max forwards header*/
            MaxForwardsHeader maxForwards = sipManager.headerFactory
                    .createMaxForwardsHeader(70);
            /*create the request*/
            Request callRequest = sipManager.messageFactory.createRequest(requestURI,
                    Request.BYE, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);
            /*
            extra headers, optional
            SupportedHeader supportedHeader = sipManager.headerFactory
                    .createSupportedHeader("replaces, outbound");
            callRequest.addHeader(supportedHeader);*/
            addCustomHeaders(callRequest, sipManager);

            SipURI routeUri = sipManager.addressFactory.createSipURI(null, DeviceImpl.getInstance().getSipProfile().getRemoteIp());
            routeUri.setTransportParam(DeviceImpl.getInstance().getSipProfile().getTransport());
            routeUri.setLrParam();
            routeUri.setPort(DeviceImpl.getInstance().getSipProfile().getRemotePort());

            Address routeAddress = sipManager.addressFactory.createAddress(routeUri);
            RouteHeader route = sipManager.headerFactory.createRouteHeader(routeAddress);
            callRequest.addHeader(route);
           /* // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = sipManager.headerFactory
                    .createContentTypeHeader("application", "sdp");
*/
            // Create the contact name address.
            SipURI contactURI = sipManager.addressFactory.createSipURI(DeviceImpl.getInstance().getSipProfile().getSipUserName(), DeviceImpl.getInstance().getSipProfile().getLocalIp());
            contactURI.setPort(sipManager.sipProvider.getListeningPoint(DeviceImpl.getInstance().getSipProfile().getTransport())
                    .getPort());

            Address contactAddress = sipManager.addressFactory.createAddress(contactURI);

            // Add the contact address.
            //contactAddress.setDisplayName(fromName);

            ContactHeader contactHeader = sipManager.headerFactory.createContactHeader(contactAddress);
            callRequest.addHeader(contactHeader);

            return callRequest;

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();

        }
        return null;
    }

    private void addCustomHeaders(Request callRequest, SipManager sipManager) {
        // Get a set of the entries
        Set set = sipManager.getCustomHeaders().entrySet();
        // Get an iterator
        Iterator i = set.iterator();
        // Display elements
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            try {
                Header customHeader = sipManager.headerFactory.createHeader(me.getKey().toString(), me.getValue().toString());
                callRequest.addHeader(customHeader);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }


    public Request buildAskListInvite(SipManager sipManager, String to) {

        try {
            /*create from header*/
            SipURI from = sipManager.addressFactory.createSipURI(DeviceImpl.getInstance().getSipProfile().getSipUserName(),
                    DeviceImpl.getInstance().getSipProfile().getLocalEndpoint());
            Address fromNameAddress = sipManager.addressFactory.createAddress(from);
            fromNameAddress.setDisplayName(DeviceImpl.getInstance().getSipProfile().getSipUserName());
            FromHeader fromHeader = sipManager.headerFactory.createFromHeader(fromNameAddress,
                    "Tzt0ZEP92");

            /*to header 此处填服务器地址*/
            URI toAddress = sipManager.addressFactory.createSipURI("server", DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint());
            Address toNameAddress = sipManager.addressFactory.createAddress(toAddress);
            toNameAddress.setDisplayName("server");
            ToHeader toHeader = sipManager.headerFactory.createToHeader(toNameAddress, null);

            /*request URI 还是写服务器*/
            // URI requestURI = sipManager.addressFactory.createURI(DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint());
            // requestURI.setTransportParam("udp");
            // create Request URI
            SipURI requestURI = sipManager.addressFactory.createSipURI("server",
                    DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint());

            /*via headers*/
            ArrayList<ViaHeader> viaHeaders = sipManager.createViaHeader();

            /*callId header*/
            CallIdHeader callIdHeader = sipManager.sipProvider.getNewCallId();

            /*Cseq header*/
            CSeqHeader cSeqHeader = sipManager.headerFactory.createCSeqHeader(1l,
                    Request.INVITE);

            /*max forwards header*/
            MaxForwardsHeader maxForwards = sipManager.headerFactory
                    .createMaxForwardsHeader(70);
            /*create the request*/
            Request callRequest = sipManager.messageFactory.createRequest(requestURI,
                    Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);
            /*
            extra headers, optional
            SupportedHeader supportedHeader = sipManager.headerFactory
                    .createSupportedHeader("replaces, outbound");
            callRequest.addHeader(supportedHeader);*/
            addCustomHeaders(callRequest, sipManager);

            SipURI routeUri = sipManager.addressFactory.createSipURI(null, DeviceImpl.getInstance().getSipProfile().getRemoteIp());
            routeUri.setTransportParam(DeviceImpl.getInstance().getSipProfile().getTransport());
            routeUri.setLrParam();
            routeUri.setPort(DeviceImpl.getInstance().getSipProfile().getRemotePort());

            Address routeAddress = sipManager.addressFactory.createAddress(routeUri);
            RouteHeader route = sipManager.headerFactory.createRouteHeader(routeAddress);
            callRequest.addHeader(route);
           /* // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = sipManager.headerFactory
                    .createContentTypeHeader("application", "sdp");
*/
            // Create the contact name address.
            SipURI contactURI = sipManager.addressFactory.createSipURI(DeviceImpl.getInstance().getSipProfile().getSipUserName(), DeviceImpl.getInstance().getSipProfile().getLocalIp());
            contactURI.setPort(sipManager.sipProvider.getListeningPoint(DeviceImpl.getInstance().getSipProfile().getTransport())
                    .getPort());

            Address contactAddress = sipManager.addressFactory.createAddress(contactURI);

            // Add the contact address.
            //contactAddress.setDisplayName(fromName);

            ContactHeader contactHeader = sipManager.headerFactory.createContactHeader(contactAddress);
            callRequest.addHeader(contactHeader);

            // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = sipManager.headerFactory
                    .createContentTypeHeader("action", "list");
            callRequest.setContent("", contentTypeHeader);// the action sends with a blank body
            return callRequest;

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();

        }
        return null;
    }
}