import netscape.javascript.JSObject;


import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.io.*;
import java.util.Arrays;

import static java.lang.invoke.ConstantBootstraps.invoke;


public class HttpServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }


        boolean running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();

                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine, outputLine;
                outputLine = null;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Recibí: " + inputLine);
                    if (inputLine.startsWith("GET /favicon.ico")) {
                        break;
                    }
                    if (inputLine.startsWith("GET /compreflex")) {
                        String command = inputLine.split("=")[1];
                        System.out.println("command:"+command);
                        command = java.net.URLDecoder.decode(command,"UTF-8");

                        try {
                            outputLine = calcular(command);
                        } catch (Exception e) {
                            outputLine = e.getMessage();
                        }
                    }
                    if (!in.ready()) {
                        break;
                    }

                    outputLine = "{\" respuesta\":\"" + outputLine +"\"}";
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: application/json");
                    out.println("Content-Length: " + outputLine.length() );
                    out.println("Connection : close");
                    out.println();
                    out.println(outputLine);


                }
                out.close();
                in.close();
                clientSocket.close();





            } catch (IOException e) {
                System.err.println("Accept failed.");
                //System.exit(1);
            }
        }
    }

    public static String calcular(String command) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if(command.contains("Class")){
            String[] parts = command.split("\\(");
            String methodName = parts[1];
            methodName = methodName.split("\\)")[0];
            System.out.println("m:"+methodName);
            try{
                Class<?> c = Class.forName(methodName);
                return Arrays.toString(c.getMethods());
            }catch(ClassNotFoundException e) {
                return e.getMessage();
            }
        }else if(command.contains("invoke")){
            command = command.replace("\\)", "");
            String[] parts = command.split("\\(");
            String[] args = (parts[1].split(","));
            String param = args[1].split("\\)")[0];
            param = param.trim();
            try{
                Class<?> c = Class.forName(args[0]);
                Method method = c.getMethod(param);

                return String.valueOf(method.invoke(null));
            }catch(ClassNotFoundException e) {
                return e.getMessage();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        else if(command.contains("unaryInvoke")){
            command = command.replace("\\)", "");
            String[] parts = command.split("\\(");
            String[] args = (parts[1].split(","));
            System.out.println(args[0]);
            Class<?> c = Class.forName(args[0]);
            String m = args[1];
            Method method = c.getMethod(m);
            String t = args[2];
            String v = args[3];

            return String.valueOf(method.invoke(t,v));
        }else if(command.contains("binaryInvoke")){
            return "";
        }
        return "";
    }


}