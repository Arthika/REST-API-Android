package com.example.arthika.arthikahft;

import org.apache.http.ConnectionClosedException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
//import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
//import org.apache.http.util.EntityUtilsHC4;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

interface ArthikaHFTPriceListener {
    void timestampEvent(String timestamp);
    void heartbeatEvent();
    void messageEvent(String message);
    void priceEvent(List<ArthikaHFT.priceTick> priceTickList);
    void assetPositionEvent(List<ArthikaHFT.assetPositionTick> assetPositionTickList);
    void securityPositionEvent(List<ArthikaHFT.securityPositionTick> securityPositionTickList);
    void positionHeartbeatEvent(ArthikaHFT.positionHeartbeat positionHeartbeat);
    void orderEvent(List<ArthikaHFT.orderTick> orderTickList);
    void orderHeartbeatEvent(ArthikaHFT.orderHeartbeat orderHeartbeat);
}

public class ArthikaHFT {

    private String	URL;
    private String	user;
    private String	password;
    private String	token = null;
    private HashMap<ThreadExecution,myResponseHandler> threadmap;

    private final static String STREAMINGURL = "cgi-bin/IHFTRestStreamer/";
    private final static String POLLINGURL = "fcgi-bin/IHFTRestAPI/";
    private final static String PORT = "81";

    public static class hftRequest {
        public getPriceRequest     getPrice;
        public getPositionRequest  getPosition;
        public getOrderRequest     getOrder;
        public setOrderRequest     setOrder;

        private hftRequest() {
        }
    }

    public static class hftResponse {
        public getPriceResponse    getPriceResponse;
        public getPositionResponse getPositionResponse;
        public getOrderResponse    getOrderResponse;
        public setOrderResponse    setOrderResponse;
    }

    public static class getPriceRequest {
        public String        user;
        public String        token;
        public List<String>  security;
        public List<String>  tinterface;
        public String        granularity;
        public int           levels;

        public getPriceRequest( String user, String token, List<String> security, List<String> tinterface, String granularity, int levels ) {
            this.user = user;
            this.token = token;
            this.security = security;
            this.tinterface = tinterface;
            this.granularity = granularity;
            this.levels = levels;
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

        public getPositionRequest( String user, String token, List<String> asset, List<String> security, List<String> account ) {
            this.user = user;
            this.token = token;
            this.asset = asset;
            this.security = security;
            this.account = account;
        }
    }

    public static class getPositionResponse {
        public int              result;
        public String           message;
        public List<assetPositionTick>  assetposition;
        public List<securityPositionTick>  securityposition;
        public positionHeartbeat  heartbeat;
        public String           timestamp;
    }

    public static class getOrderRequest {
        public String        user;
        public String        token;
        public List<String>  security;
        public List<String>  tinterface;
        public List<String>  type;

        public getOrderRequest( String user, String token, List<String> security, List<String> tinterface, List<String> type ) {
            this.user = user;
            this.token = token;
            this.security = security;
            this.tinterface = tinterface;
            this.type = type;
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
        public double  equity;
        public double  totalexposure;
        public double  freemargin;
    }

    public static class securityPositionTick {
        public String  account;
        public String  security;
        public double  exposure;
        public String  side;
        public double  price;
        public int     pips;
        public double  equity;
        public double  freemargin;
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
    }

    public static class positionTick {
        public List<assetPositionTick> assetPositionTickList;
        public List<securityPositionTick> securityPositionTickList;
    }

    public class myResponseHandler implements ResponseHandler {

        private ObjectMapper mapper;
        private boolean stream = true;
        private List<priceTick> priceTickList = new ArrayList<priceTick>();
        private List<assetPositionTick> assetPositionTickList = new ArrayList<assetPositionTick>();
        private List<securityPositionTick> securityPositionTickList = new ArrayList<securityPositionTick>();
        private List<orderRequest> orderList = new ArrayList<orderRequest>();
        private List<orderTick> orderTickList = new ArrayList<orderTick>();
        public ArthikaHFTPriceListener listener;

        public void setObjectMapper(ObjectMapper mapper){
            this.mapper = mapper;
        }

        public List<priceTick> getPriceTickList(){
            return priceTickList;
        }

        public List<assetPositionTick> getAssetPositionTickList(){
            return assetPositionTickList;
        }

        public List<securityPositionTick> getSecurityPositionTickList(){
            return securityPositionTickList;
        }

        public List<orderTick> getOrderTickList() {
            return orderTickList;
        }

        public List<orderRequest> getOrderList() {
            return orderList;
        }

        public void setStream(boolean stream){
            this.stream = stream;
        }

        public String handleResponse(final HttpResponse httpresponse) throws IOException {
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

    public ArthikaHFT(){
        init();
    }

    public ArthikaHFT(String URL){
        this.URL = URL;
        init();
    }

    public void init(){
        threadmap = new HashMap<ThreadExecution,myResponseHandler>();
    }

    public void doAuthentication(String user, String password){
        this.user = user;
        this.password = password;
        // TODO generate token
        this.token = this.password;
    }

    public List<priceTick> getPrice(List<String> securities, List<String> tinterfaces, String granularity, int levels) throws IOException, InterruptedException {
        hftRequest hftrequest = new hftRequest();
        hftrequest.getPrice = new getPriceRequest(user, token, securities, tinterfaces, granularity, levels);
        myResponseHandler responseHandler = new myResponseHandler();
        sendRequest(hftrequest, responseHandler, "getPrice", false, null);
        return responseHandler.getPriceTickList();
    }

    public long getPriceBegin(List<String> securities, List<String> tinterfaces, String granularity, int levels, ArthikaHFTPriceListener listener ) throws IOException, InterruptedException {
        hftRequest hftrequest = new hftRequest();
        hftrequest.getPrice = new getPriceRequest(user, token, securities, tinterfaces, granularity, levels);
        myResponseHandler responseHandler = new myResponseHandler();
        return sendRequest(hftrequest, responseHandler, "getPrice", true, listener);
    }

    public boolean getPriceEnd(long threadid) throws IOException {
        return finishStreaming(threadid);
    }

    public positionTick getPosition(List<String> assets, List<String> securities, List<String> accounts) throws IOException, InterruptedException {
        hftRequest hftrequest = new hftRequest();
        hftrequest.getPosition = new getPositionRequest(user, token, assets, securities, accounts);
        myResponseHandler responseHandler = new myResponseHandler();
        sendRequest(hftrequest, responseHandler, "getPosition", false, null);
        positionTick positiontick = new positionTick();
        positiontick.assetPositionTickList = responseHandler.assetPositionTickList;
        positiontick.securityPositionTickList = responseHandler.securityPositionTickList;
        return positiontick;
    }

    public long getPositionBegin(List<String> assets, List<String> securities, List<String> accounts, ArthikaHFTPriceListener listener ) throws IOException, InterruptedException {
        hftRequest hftrequest = new hftRequest();
        hftrequest.getPosition = new getPositionRequest(user, token, assets, securities, accounts);
        myResponseHandler responseHandler = new myResponseHandler();
        return sendRequest(hftrequest, responseHandler, "getPosition", true, listener);

    }

    public boolean getPositionEnd(long threadid) throws IOException {
        return finishStreaming(threadid);
    }

    public List<orderTick> getOrder(List<String> securities, List<String> tinterfaces, List<String> types) throws IOException, InterruptedException {
        hftRequest hftrequest = new hftRequest();
        hftrequest.getOrder = new getOrderRequest(user, token, securities, tinterfaces, types);
        myResponseHandler responseHandler = new myResponseHandler();
        sendRequest(hftrequest, responseHandler, "getOrder", false, null);
        return responseHandler.getOrderTickList();
    }

    public long getOrderBegin(List<String> securities, List<String> tinterfaces, List<String> types, ArthikaHFTPriceListener listener ) throws IOException, InterruptedException {
        hftRequest hftrequest = new hftRequest();
        hftrequest.getOrder = new getOrderRequest(user, token, securities, tinterfaces, types);
        myResponseHandler responseHandler = new myResponseHandler();
        return sendRequest(hftrequest, responseHandler, "getOrder", true, listener);
    }

    public boolean getOrderEnd(long threadid) throws IOException {
        return finishStreaming(threadid);
    }

    public List<orderRequest> setOrder(List<orderRequest> orders) throws IOException, InterruptedException {
        hftRequest hftrequest = new hftRequest();
        hftrequest.setOrder = new setOrderRequest(user, token, orders);
        //BasicResponseHandler responseHandler = new BasicResponseHandler();
        myResponseHandler responseHandler = new myResponseHandler();
        sendRequest(hftrequest, responseHandler, "setOrder", false, null);
        return responseHandler.getOrderList();
    }

    private long sendRequest(hftRequest hftrequest, myResponseHandler responseHandler, String urlpath, boolean stream, ArthikaHFTPriceListener listener) throws IOException, InterruptedException {
        if (token==null){
            // TODO error
            return -1;
        }

        final ObjectMapper mapper = new ObjectMapper();
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") );
        headers.add(new BasicHeader(HttpHeaders.ACCEPT, "application/json"));
        CloseableHttpClient client = HttpClients.custom().setDefaultHeaders(headers).build();

        mapper.setSerializationInclusion(Inclusion.NON_NULL);
        mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(SerializationConfig.Feature.SORT_PROPERTIES_ALPHABETICALLY, false);
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        StringEntity request = new StringEntity(mapper.writeValueAsString(hftrequest));
        if (stream) {
            request = new StringEntity("{\"getPrice\":{\"user\":\"fedenice\",\"token\":\"fedenice\",\"security\":[\"EUR_USD\", \"EUR_GBP\", \"EUR_JPY\", \"USD_JPY\", \"GBP_USD\", \"GBP_JPY\", \"AUD_USD\", \"USD_CAD\"],\"tinterface\":[\"Baxter_CNX\",\"Cantor_CNX_3\"],\"granularity\":\"TOB\",\"levels\":1}}");
        }
        else{
            if(urlpath.equals("setOrder")) {
                request = new StringEntity("{\"setOrder\":{\"user\":\"fedenice\",\"token\":\"fedenice\",\"order\":[{\"security\":\"EUR_USD\",\"tinterface\":\"Baxter_CNX\",\"quantity\":5000,\"side\":\"buy\",\"type\":\"market\",\"price\":0.0,\"expiration\":0,\"userparam\":0,\"tempid\":0}]}}");
            }
            if(urlpath.equals("getOrder")) {
                request = new StringEntity("{\"getOrder\":{\"user\":\"fedenice\",\"token\":\"fedenice\"}}");
            }
        }
        System.out.println(mapper.writeValueAsString(hftrequest));
        responseHandler.setObjectMapper(mapper);
        responseHandler.setStream(stream);
        HttpPost httpRequest;
        if (stream){
            httpRequest = new HttpPost(URL + ":" + PORT + "/" + STREAMINGURL + urlpath);
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
            httpRequest = new HttpPost(URL + ":" + PORT + "/" + POLLINGURL + urlpath);
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