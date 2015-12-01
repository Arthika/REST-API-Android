package com.example.arthika.arthikahft;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.ConnectionClosedException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

interface ArthikaHFTPriceListener {
    void timestampEvent(String timestamp);
    void heartbeatEvent();
    void messageEvent(String message);
    void priceEvent(List<ArthikaHFT.priceTick> priceTickList);
    void accountingEvent(ArthikaHFT.accountingTick accountingTick);
    void assetPositionEvent(List<ArthikaHFT.assetPositionTick> assetPositionTickList);
    void securityPositionEvent(List<ArthikaHFT.securityPositionTick> securityPositionTickList);
    void positionHeartbeatEvent(ArthikaHFT.positionHeartbeat positionHeartbeat);
    void orderEvent(List<ArthikaHFT.orderTick> orderTickList);
    void orderHeartbeatEvent(ArthikaHFT.orderHeartbeat orderHeartbeat);
}

public class ArthikaHFT {

    private boolean ssl;
    private String domain;
    private String url_stream;
    private String url_polling;
    private String url_challenge;
    private String url_token;
    private String user;
    private String password;
    private String authentication_port;
    private String request_port;
    private String ssl_cert;
    private String challenge;
    private String token = null;
    private SSLContext sslContext = null;

    private HashMap<ThreadExecution,myResponseHandler> threadmap;
    static final Object wrapperLock = new Object();

    public static final String SIDE_BUY = "buy";
    public static final String SIDE_SELL = "sell";
    public static final String SIDE_ASK = "ask";
    public static final String SIDE_BID = "bid";
    public static final String TYPE_MARKET = "market";
    public static final String TYPE_LIMIT = "limit";
    public static final String VALIDITY_DAY = "day";
    public static final String VALIDITY_FILLORKILL = "fill or kill";
    public static final String VALIDITY_INMEDIATEORCANCEL = "inmediate or cancel";
    public static final String VALIDITY_GOODTILLCANCEL = "good till cancel";
    public static final String GRANULARITY_TOB = "tob";
    //public static final String GRANULARITY_FAB = "fab";

    public static final String ORDERTYPE_PENDING = "pending";
    //public static final String ORDERTYPE_INDETERMINATED = "indetermined";
    //public static final String ORDERTYPE_EXECUTED = "executed";
    //public static final String ORDERTYPE_CANCELED = "canceled";
    //public static final String ORDERTYPE_REJECTED = "rejected";
    public static final String ORDERTYPE_INFLUX = "in flux";

    public static class hftRequest {
        public getAuthorizationChallengeRequest getAuthorizationChallenge;
        public getAuthorizationTokenRequest     getAuthorizationToken;
        public getAccountRequest                getAccount;
        public getInterfaceRequest              getInterface;
        public getPriceRequest     getPrice;
        public getPositionRequest  getPosition;
        public getOrderRequest     getOrder;
        public setOrderRequest     setOrder;
        public cancelOrderRequest  cancelOrder;
        public modifyOrderRequest  modifyOrder;

        private hftRequest() {
        }
    }

    public static class hftResponse {
        public getAuthorizationChallengeResponse getAuthorizationChallengeResponse;
        public getAuthorizationTokenResponse     getAuthorizationTokenResponse;
        public getAccountResponse                getAccountResponse;
        public getInterfaceResponse              getInterfaceResponse;
        public getPriceResponse    getPriceResponse;
        public getPositionResponse getPositionResponse;
        public getOrderResponse    getOrderResponse;
        public setOrderResponse    setOrderResponse;
        public cancelOrderResponse cancelOrderResponse;
        public modifyOrderResponse modifyOrderResponse;
    }

    public static class getAuthorizationChallengeRequest {
        public String        user;

        public getAuthorizationChallengeRequest( String user ) {
            this.user = user;
        }
    }

    public static class getAuthorizationChallengeResponse {
        public String        challenge;
        public String        timestamp;
    }

    public static class getAuthorizationTokenRequest {
        public String        user;
        public String        challengeresp;

        public getAuthorizationTokenRequest( String user, String challengeresp ) {
            this.user = user;
            this.challengeresp = challengeresp;
        }
    }

    public static class getAuthorizationTokenResponse {
        public String        token;
        public String        timestamp;
    }

    public static class getAccountRequest {
        public String        user;
        public String        token;

        public getAccountRequest( String user, String token ) {
            this.user = user;
            this.token = token;
        }
    }

    public static class getAccountResponse {
        public List<accountTick>    account;
        public String               timestamp;
    }

    public static class getInterfaceRequest {
        public String        user;
        public String        token;

        public getInterfaceRequest( String user, String token ) {
            this.user = user;
            this.token = token;
        }
    }

    public static class getInterfaceResponse {
        public List<tinterfaceTick> tinterface;
        public String               timestamp;
    }

    public static class getPriceRequest {
        public String        user;
        public String        token;
        public List<String>  security;
        public List<String>  tinterface;
        public String        granularity;
        public int           levels;
        public int           interval;

        public getPriceRequest( String user, String token, List<String> security, List<String> tinterface, String granularity, int levels, int interval ) {
            this.user = user;
            this.token = token;
            this.security = security;
            this.tinterface = tinterface;
            this.granularity = granularity;
            this.levels = levels;
            this.interval = interval;
        }
    }

    public static class getPriceResponse {
        public int              result;
        public String           message;
        public List<priceTick>  tick;
        public priceHeartbeat   heartbeat;
        public String           timestamp;
    }

    public static class getPositionRequest {
        public String        user;
        public String        token;
        public List<String>  asset;
        public List<String>  security;
        public List<String>  account;
        public int           interval;

        public getPositionRequest( String user, String token, List<String> asset, List<String> security, List<String> account, int interval ) {
            this.user = user;
            this.token = token;
            this.asset = asset;
            this.security = security;
            this.account = account;
            this.interval = interval;
        }
    }

    public static class getPositionResponse {
        public int              result;
        public String           message;
        public List<assetPositionTick>  assetposition;
        public List<securityPositionTick>  securityposition;
        public accountingTick   accounting;
        public positionHeartbeat  heartbeat;
        public String           timestamp;
    }

    public static class getOrderRequest {
        public String        user;
        public String        token;
        public List<String>  security;
        public List<String>  tinterface;
        public List<String>  type;
        public int           interval;

        public getOrderRequest( String user, String token, List<String> security, List<String> tinterface, List<String> type, int interval ) {
            this.user = user;
            this.token = token;
            this.security = security;
            this.tinterface = tinterface;
            this.type = type;
            this.interval = interval;
        }
    }

    public static class getOrderResponse {
        public int              result;
        public String           message;
        public List<orderTick>  order;
        public orderHeartbeat   heartbeat;
        public String           timestamp;

    }

    public static class setOrderRequest {
        public String        user;
        public String        token;
        public List<orderRequest>  order;

        public setOrderRequest( String user, String token, List<orderRequest> order ) {
            this.user = user;
            this.token = token;
            this.order = order;
        }
    }

    public static class setOrderResponse {
        public int              result;
        public String           message;
        public List<orderRequest>    order;
        public String           timestamp;
    }

    public static class cancelOrderRequest {
        public String        user;
        public String        token;
        public List<String>  fixid;

        public cancelOrderRequest( String user, String token, List<String> fixid ) {
            this.user = user;
            this.token = token;
            this.fixid = fixid;
        }
    }

    public static class cancelOrderResponse {
        public List<cancelTick> order;
        public String           message;
        public String           timestamp;
    }

    public static class modifyOrderRequest {
        public String        user;
        public String        token;
        public List<modOrder>   order;

        public modifyOrderRequest( String user, String token, List<modOrder> order ) {
            this.user = user;
            this.token = token;
            this.order = order;
        }
    }

    public static class modifyOrderResponse {
        public List<modifyTick> order;
        public String           message;
        public String           timestamp;
    }

    public static class accountTick {
        public String        name;
        public String        description;
        public String        style;
        public int           leverage;
        public String        rollover;
        public String        settlement;
    }

    public static class tinterfaceTick {
        public String        name;
        public String        description;
        public String        account;
        public String        commissions;
    }

    public static class priceTick {
        public String  security;
        public String  tinterface;
        public double  price;
        public int     pips;
        public int     liquidity;
        public String  side;
    }

    public static class priceHeartbeat {
        public List<String>  security;
        public List<String>  tinterface;
    }

    public static class assetPositionTick {
        public String  account;
        public String  asset;
        public double  exposure;
        public double  totalrisk;
        public double  pl;
    }

    public static class securityPositionTick {
        public String  account;
        public String  security;
        public double  exposure;
        public String  side;
        public double  price;
        public int     pips;
        public double  pl;
    }

    public static class accountingTick {
        public double  strategyPL;
        public double  totalequity;
        public double  usedmargin;
        public double  freemargin;
        public String  m2mcurrency;
    }

    public static class positionHeartbeat {
        public List<String>  asset;
        public List<String>  security;
        public List<String>  account;
    }

    public static class orderTick {
        public int     tempid;
        public String  orderid;
        public String  fixid;
        public String  account;
        public String  tinterface;
        public String  security;
        public int     pips;
        public int     quantity;
        public String  side;
        public String  type;
        public double  limitprice;
        public int     maxshowquantity;
        public String  timeinforce;
        public int     seconds;
        public int     milliseconds;
        public String  expiration;
        public double  finishedprice;
        public int     finishedquantity;
        public String  commcurrency;
        public double  commission;
        public double  priceatstart;
        public int     userparam;
        public String  status;
        public String  reason;
    }

    public static class orderHeartbeat {
        public List<String>  security;
        public List<String>  tinterface;
    }

    public static class orderRequest {
        public String  security;
        public String  tinterface;
        public int     quantity;
        public String  side;
        public String  type;
        public String  timeinforce;
        public double  price;
        public int     expiration;
        public int     userparam;
        public int     tempid;
        public String  result;
    }

    public static class positionTick {
        public List<assetPositionTick> assetPositionTickList;
        public List<securityPositionTick> securityPositionTickList;
        public accountingTick accountingTick;
    }

    public static class cancelTick {
        public String  fixid;
        public String  result;
    }

    public static class modOrder {
        public String  fixid;
        public double  price;
        public int     quantity;
    }

    public static class modifyTick {
        public String  fixid;
        public String  result;
    }

    public class myResponseHandler implements ResponseHandler<String> {

        private ObjectMapper mapper;
        private boolean stream = true;
        private List<accountTick> accountTickList = new ArrayList<accountTick>();
        private List<tinterfaceTick> tinterfaceTickList = new ArrayList<tinterfaceTick>();
        private List<priceTick> priceTickList = new ArrayList<priceTick>();
        private accountingTick accountingTick = new accountingTick();
        private List<assetPositionTick> assetPositionTickList = new ArrayList<assetPositionTick>();
        private List<securityPositionTick> securityPositionTickList = new ArrayList<securityPositionTick>();
        private List<orderRequest> orderList = new ArrayList<orderRequest>();
        private List<orderTick> orderTickList = new ArrayList<orderTick>();
        private List<cancelTick> cancelTickList = new ArrayList<cancelTick>();
        private List<modifyTick> modifyTickList = new ArrayList<modifyTick>();
        public ArthikaHFTPriceListener listener;

        public void setObjectMapper(ObjectMapper mapper){
            this.mapper = mapper;
        }

        public List<accountTick> getAccountTickList(){
            return accountTickList;
        }

        public List<tinterfaceTick> getTinterfaceTickList(){
            return tinterfaceTickList;
        }

        public List<priceTick> getPriceTickList(){
            return priceTickList;
        }

        public accountingTick getAccountingTick(){
            return accountingTick;
        }

        public List<assetPositionTick> getAssetPositionTickList(){
            return assetPositionTickList;
        }

        public List<securityPositionTick> getSecurityPositionTickList() {
            return securityPositionTickList;
        }

        public List<orderTick> getOrderTickList() {
            return orderTickList;
        }

        public List<orderRequest> getOrderList() {
            return orderList;
        }

        public List<cancelTick> getCancelList() {
            return cancelTickList;
        }

        public List<modifyTick> getModifyList() {
            return modifyTickList;
        }

        public void setStream(boolean stream){
            this.stream = stream;
        }

        @Override
        public String handleResponse(final HttpResponse httpresponse) throws ClientProtocolException, IOException {
            int status = httpresponse.getStatusLine().getStatusCode();
            BufferedReader bufferedReader;
            if (status >= 200 && status < 300) {
                HttpEntity entity = httpresponse.getEntity();

                // --------------------------------------------------------------
                // Wait for continuous responses from server (streaming/polling)
                // --------------------------------------------------------------

                try {
                    InputStreamReader stream = new InputStreamReader(entity.getContent());
                    bufferedReader = new BufferedReader(stream);
                    String line = null;

                    while ((line = bufferedReader.readLine()) != null) {

                        hftResponse response = null;
                        try{
                            response = mapper.readValue(line, hftResponse.class);
                        }
                        catch (Exception ex){
                            System.out.println("Error reading: " + line);
                            throw ex;
                        }

                        if (response.getAuthorizationChallengeResponse != null){
                            challenge = response.getAuthorizationChallengeResponse.challenge;
                            return null;
                        }
                        if (response.getAuthorizationTokenResponse != null){
                            token = response.getAuthorizationTokenResponse.token;
                            return null;
                        }

                        if (response.getAccountResponse!=null){
                            if (response.getAccountResponse.account != null){
                                for (accountTick tick : response.getAccountResponse.account){
                                    accountTickList.add(tick);
                                }
                            }
                        }
                        if (response.getInterfaceResponse!=null){
                            if (response.getInterfaceResponse.tinterface != null){
                                for (tinterfaceTick tick : response.getInterfaceResponse.tinterface){
                                    tinterfaceTickList.add(tick);
                                }
                            }
                        }
                        if (response.getPriceResponse!=null){
                            if(this.stream){
                                if (response.getPriceResponse.timestamp != null){
                                    listener.timestampEvent(response.getPriceResponse.timestamp);
                                }
                                if (response.getPriceResponse.tick != null){
                                    listener.priceEvent(response.getPriceResponse.tick);
                                }
                                if (response.getPriceResponse.heartbeat != null){
                                    listener.heartbeatEvent();
                                }
                                if (response.getPriceResponse.message != null){
                                    listener.messageEvent(response.getPriceResponse.message);
                                }
                            }
                            else{
                                if (response.getPriceResponse.tick != null){
                                    for (priceTick tick : response.getPriceResponse.tick){
                                        priceTickList.add(tick);
                                    }
                                }
                            }
                        }
                        if (response.getPositionResponse!=null){
                            if(this.stream){
                                if (response.getPositionResponse.timestamp != null){
                                    listener.timestampEvent(response.getPositionResponse.timestamp);
                                }
                                if (response.getPositionResponse.accounting!= null){
                                    listener.accountingEvent(response.getPositionResponse.accounting);
                                }
                                if (response.getPositionResponse.assetposition!= null){
                                    listener.assetPositionEvent(response.getPositionResponse.assetposition);
                                }
                                if (response.getPositionResponse.securityposition!= null){
                                    listener.securityPositionEvent(response.getPositionResponse.securityposition);
                                }
                                if (response.getPositionResponse.heartbeat!= null){
                                    listener.positionHeartbeatEvent(response.getPositionResponse.heartbeat);
                                }
                                if (response.getPositionResponse.message != null){
                                    listener.messageEvent(response.getPositionResponse.message);
                                }
                            }
                            else{
                                if (response.getPositionResponse.accounting != null){
                                    accountingTick = response.getPositionResponse.accounting;
                                }
                                if (response.getPositionResponse.assetposition != null){
                                    for (assetPositionTick tick : response.getPositionResponse.assetposition){
                                        assetPositionTickList.add(tick);
                                    }
                                }
                                if (response.getPositionResponse.securityposition != null){
                                    for (securityPositionTick tick : response.getPositionResponse.securityposition){
                                        securityPositionTickList.add(tick);
                                    }
                                }
                            }
                        }
                        if (response.getOrderResponse!=null){
                            if(this.stream){
                                if (response.getOrderResponse.timestamp != null){
                                    listener.timestampEvent(response.getOrderResponse.timestamp);
                                }
                                if (response.getOrderResponse.order!= null){
                                    listener.orderEvent(response.getOrderResponse.order);
                                }
                                if (response.getOrderResponse.heartbeat!= null){
                                    listener.orderHeartbeatEvent(response.getOrderResponse.heartbeat);
                                }
                                if (response.getOrderResponse.message != null){
                                    listener.messageEvent(response.getOrderResponse.message);
                                }
                            }
                            else{
                                if (response.getOrderResponse.order != null){
                                    for (orderTick tick : response.getOrderResponse.order){
                                        orderTickList.add(tick);
                                    }
                                }
                            }
                        }
                        if (response.setOrderResponse!=null){
                            if (response.setOrderResponse.timestamp != null){
                                //listener.timestampEvent(response.setOrderResponse.timestamp);
                            }
                            if (response.setOrderResponse.order != null){
                                this.orderList = response.setOrderResponse.order;
                            }
                            if (response.setOrderResponse.message != null){
                                //listener.messageEvent(response.setOrderResponse.message);
                            }
                        }
                        if (response.cancelOrderResponse!=null){
                            if (response.cancelOrderResponse.timestamp != null){
                                //listener.timestampEvent(response.cancelOrderResponse.timestamp);
                            }
                            if (response.cancelOrderResponse.order != null){
                                this.cancelTickList = response.cancelOrderResponse.order;
                            }
                            if (response.cancelOrderResponse.message != null){
                                //listener.messageEvent(response.cancelOrderResponse.message);
                            }
                        }
                        if (response.modifyOrderResponse!=null){
                            if (response.modifyOrderResponse.timestamp != null){
                                //listener.timestampEvent(response.modifyOrderResponse.timestamp);
                            }
                            if (response.modifyOrderResponse.order != null){
                                this.modifyTickList = response.modifyOrderResponse.order;
                            }
                            if (response.modifyOrderResponse.message != null){
                                //listener.messageEvent(response.modifyOrderResponse.message);
                            }
                        }
                    }
                }
                catch (IOException e) {
                    // Important: When connection is locally closed 'ConnectionClosedException' will be triggered
                    //e.printStackTrace();
                    return null;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }

                if(this.stream){
                    return entity != null ? EntityUtils.toString(entity) : null;
                }
                else{
                    return null;
                }

            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        }

    }

    public ArthikaHFT(String domain, String url_stream, String url_polling, String url_challenge, String url_token, String user, String password, String authentication_port, String request_port, boolean ssl, String ssl_cert){
        this.token = null;
        this.domain = domain;
        this.url_stream = url_stream;
        this.url_polling = url_polling;
        this.url_challenge = url_challenge;
        this.url_token = url_token;
        this.user = user;
        this.password = password;
        this.authentication_port = authentication_port;
        this.request_port = request_port;
        this.ssl = ssl;
        this.ssl_cert = ssl_cert;
        init();
    }

    public void init(){
        threadmap = new HashMap<ThreadExecution,myResponseHandler>();
    }

    public void doAuthentication() throws IOException, InterruptedException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, DecoderException {
        myResponseHandler responseHandler = new myResponseHandler();
        final ObjectMapper mapper = new ObjectMapper();
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"));
        headers.add(new BasicHeader(HttpHeaders.ACCEPT, "application/json"));
        CloseableHttpClient client=null;
        if (ssl){
            // get certificate
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            URL url = new URL(ssl_cert);
            URLConnection connection = url.openConnection();
            InputStream in = connection.getInputStream();
            Certificate cert = cf.generateCertificate(in);
            //System.out.println("Cert:\n===================\n" + cert.getPublicKey().toString() + "\n");
            in.close();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null); // You don't need the KeyStore instance to come from a file.
            ks.setCertificateEntry("cert", cert);
            tmf.init(ks);
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            client = HttpClients.custom().setSslcontext(sslContext).setDefaultHeaders(headers).build();
        }
        else{
            client = HttpClients.custom().setDefaultHeaders(headers).build();
        }

        try{
            hftRequest hftrequest;
            StringEntity request;
            HttpPost httpRequest;

            // get challenge
            hftrequest = new hftRequest();
            hftrequest.getAuthorizationChallenge = new getAuthorizationChallengeRequest(user);
            mapper.setSerializationInclusion(Inclusion.NON_NULL);
            mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            request = new StringEntity(mapper.writeValueAsString(hftrequest));
            System.out.println(mapper.writeValueAsString(hftrequest));
            responseHandler.setObjectMapper(mapper);
            responseHandler.setStream(false);
            httpRequest = new HttpPost(domain + ":" + authentication_port + url_challenge);
            httpRequest.setEntity(request);
            client.execute(httpRequest, responseHandler);

            // create challenge response
            byte[] a = Hex.decodeHex(challenge.toCharArray());
            byte[] b = password.getBytes();
            byte[] c = new byte[a.length + b.length];
            System.arraycopy(a, 0, c, 0, a.length);
            System.arraycopy(b, 0, c, a.length, b.length);
            byte[] d = DigestUtils.sha1(c);
            String challengeresp = String.valueOf(Hex.encodeHex(d));

            // get token with challenge response
            hftrequest = new hftRequest();
            hftrequest.getAuthorizationToken = new getAuthorizationTokenRequest(user, challengeresp);
            mapper.setSerializationInclusion(Inclusion.NON_NULL);
            mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            request = new StringEntity(mapper.writeValueAsString(hftrequest));
            System.out.println(mapper.writeValueAsString(hftrequest));
            responseHandler.setObjectMapper(mapper);
            responseHandler.setStream(false);
            httpRequest = new HttpPost(domain + ":" + authentication_port + url_token);
            httpRequest.setEntity(request);
            client.execute(httpRequest, responseHandler);
        } finally {
            synchronized (wrapperLock) {
                wrapperLock.notify();
            }
            client.close();
        }
    }

    public List<accountTick> getAccount() throws IOException, InterruptedException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        hftRequest hftrequest = new hftRequest();
        hftrequest.getAccount = new getAccountRequest(user, token);
        myResponseHandler responseHandler = new myResponseHandler();
        sendRequest(hftrequest, responseHandler, "/getAccount", false, null);
        return responseHandler.getAccountTickList();
    }

    public List<tinterfaceTick> getInterface() throws IOException, InterruptedException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        hftRequest hftrequest = new hftRequest();
        hftrequest.getInterface = new getInterfaceRequest(user, token);
        myResponseHandler responseHandler = new myResponseHandler();
        sendRequest(hftrequest, responseHandler, "/getInterface", false, null);
        return responseHandler.getTinterfaceTickList();
    }

    public List<priceTick> getPrice(List<String> securities, List<String> tinterfaces, String granularity, int levels) throws IOException, InterruptedException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        hftRequest hftrequest = new hftRequest();
        hftrequest.getPrice = new getPriceRequest(user, token, securities, tinterfaces, granularity, levels, 0);
        myResponseHandler responseHandler = new myResponseHandler();
        sendRequest(hftrequest, responseHandler, "/getPrice", false, null);
        return responseHandler.getPriceTickList();
    }

    public long getPriceBegin(List<String> securities, List<String> tinterfaces, String granularity, int levels, int interval, ArthikaHFTPriceListener listener ) throws IOException, InterruptedException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        hftRequest hftrequest = new hftRequest();
        hftrequest.getPrice = new getPriceRequest(user, token, securities, tinterfaces, granularity, levels, interval);
        myResponseHandler responseHandler = new myResponseHandler();
        return sendRequest(hftrequest, responseHandler, "/getPrice", true, listener);
    }

    public boolean getPriceEnd(long threadid) throws IOException {
        return finishStreaming(threadid);
    }

    public positionTick getPosition(List<String> assets, List<String> securities, List<String> accounts) throws IOException, InterruptedException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        hftRequest hftrequest = new hftRequest();
        hftrequest.getPosition = new getPositionRequest(user, token, assets, securities, accounts, 0);
        myResponseHandler responseHandler = new myResponseHandler();
        sendRequest(hftrequest, responseHandler, "/getPosition", false, null);
        positionTick positiontick = new positionTick();
        positiontick.assetPositionTickList = responseHandler.getAssetPositionTickList();
        positiontick.securityPositionTickList = responseHandler.getSecurityPositionTickList();
        positiontick.accountingTick = responseHandler.getAccountingTick();
        return positiontick;
    }

    public long getPositionBegin(List<String> assets, List<String> securities, List<String> accounts, int interval, ArthikaHFTPriceListener listener ) throws IOException, InterruptedException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        hftRequest hftrequest = new hftRequest();
        hftrequest.getPosition = new getPositionRequest(user, token, assets, securities, accounts, interval);
        myResponseHandler responseHandler = new myResponseHandler();
        return sendRequest(hftrequest, responseHandler, "/getPosition", true, listener);

    }

    public boolean getPositionEnd(long threadid) throws IOException {
        return finishStreaming(threadid);
    }

    public List<orderTick> getOrder(List<String> securities, List<String> tinterfaces, List<String> types) throws IOException, InterruptedException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        hftRequest hftrequest = new hftRequest();
        hftrequest.getOrder = new getOrderRequest(user, token, securities, tinterfaces, types, 0);
        myResponseHandler responseHandler = new myResponseHandler();
        sendRequest(hftrequest, responseHandler, "/getOrder", false, null);
        return responseHandler.getOrderTickList();
    }

    public long getOrderBegin(List<String> securities, List<String> tinterfaces, List<String> types, int interval, ArthikaHFTPriceListener listener ) throws IOException, InterruptedException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        hftRequest hftrequest = new hftRequest();
        hftrequest.getOrder = new getOrderRequest(user, token, securities, tinterfaces, types, interval);
        myResponseHandler responseHandler = new myResponseHandler();
        return sendRequest(hftrequest, responseHandler, "/getOrder", true, listener);
    }

    public boolean getOrderEnd(long threadid) throws IOException {
        return finishStreaming(threadid);
    }

    public List<orderRequest> setOrder(List<orderRequest> orders) throws IOException, InterruptedException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        hftRequest hftrequest = new hftRequest();
        hftrequest.setOrder = new setOrderRequest(user, token, orders);
        myResponseHandler responseHandler = new myResponseHandler();
        sendRequest(hftrequest, responseHandler, "/setOrder", false, null);
        return responseHandler.getOrderList();
    }

    public List<cancelTick> cancelOrder(List<String> orders) throws IOException, InterruptedException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        hftRequest hftrequest = new hftRequest();
        hftrequest.cancelOrder = new cancelOrderRequest(user, token, orders);
        myResponseHandler responseHandler = new myResponseHandler();
        sendRequest(hftrequest, responseHandler, "/cancelOrder", false, null);
        return responseHandler.getCancelList();
    }

    public List<modifyTick> modifyOrder(List<modOrder> orders) throws IOException, InterruptedException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        hftRequest hftrequest = new hftRequest();
        hftrequest.modifyOrder = new modifyOrderRequest(user, token, orders);
        myResponseHandler responseHandler = new myResponseHandler();
        sendRequest(hftrequest, responseHandler, "/modifyOrder", false, null);
        return responseHandler.getModifyList();
    }

    private long sendRequest(hftRequest hftrequest, myResponseHandler responseHandler, String urlpath, boolean stream, ArthikaHFTPriceListener listener) throws IOException, InterruptedException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        if (token==null){
            return -1;
        }

        final ObjectMapper mapper = new ObjectMapper();
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"));
        headers.add(new BasicHeader(HttpHeaders.ACCEPT, "application/json"));
        CloseableHttpClient client=null;
        if (ssl){
            client = HttpClients.custom().setSslcontext(sslContext).setDefaultHeaders(headers).build();
        }
        else{
            client = HttpClients.custom().setDefaultHeaders(headers).build();
        }

        mapper.setSerializationInclusion(Inclusion.NON_NULL);
        mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        StringEntity request = new StringEntity(mapper.writeValueAsString(hftrequest).replaceAll("\n",""));
        System.out.println(mapper.writeValueAsString(hftrequest).replace("\n",""));
        responseHandler.setObjectMapper(mapper);
        responseHandler.setStream(stream);
        HttpPost httpRequest;
        if (stream){
            httpRequest = new HttpPost(domain + ":" + request_port + "/" + url_stream + urlpath);
            httpRequest.setEntity(request);
            responseHandler.listener = listener;
            ThreadExecution T = new ThreadExecution(client, httpRequest, responseHandler);
            synchronized(threadmap){
                threadmap.put(T, responseHandler);
            }
            T.start();
            return T.getId();
        }
        else{
            httpRequest = new HttpPost(domain + ":" + request_port + "/" + url_polling + urlpath);
            httpRequest.setEntity(request);
            client.execute(httpRequest, responseHandler);
        }

        return 0;
    }

    private boolean finishStreaming(long threadid) throws IOException {
        System.out.println("Ending " + threadid);
        ThreadExecution thread = null;
        synchronized(threadmap){
            Iterator<ThreadExecution> it = threadmap.keySet().iterator();
            while (it.hasNext()){
                thread = it.next();
                if (thread.getId()==threadid){
                    break;
                }
            }
        }
        if (thread==null){
            return false;
        }
        thread.stopExecution();
        return true;
    }

    class ThreadExecution extends Thread {
        private CloseableHttpClient client;
        private HttpPost httpRequest;
        private myResponseHandler responseHandler;

        ThreadExecution(CloseableHttpClient client, HttpPost httpRequest, myResponseHandler responseHandler){
            this.client = client;
            this.httpRequest = httpRequest;
            this.responseHandler = responseHandler;
            System.out.println("Creating ");
        }

        public void run() {
            System.out.println("Running " +  this.getId() );
            try {
                System.out.println("START");
                client.execute(httpRequest, responseHandler);
                synchronized(threadmap){
                    threadmap.remove(this);
                }
                System.out.println("FINISH");
            } catch (ConnectionClosedException e) {
                // Important: When connection is locally closed 'ConnectionClosedException' will be triggered
                synchronized(threadmap){
                    threadmap.remove(this);
                }
                System.out.println("FINISH");
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
            }
        }

        public void stopExecution() {
            try {
                client.close();
                synchronized(threadmap){
                    threadmap.remove(this);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
