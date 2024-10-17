package ai.latta.spring.wrappers;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class CapturingResponseWrapper extends HttpServletResponseWrapper {
    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private ServletOutputStream outputStream;
    private PrintWriter writer;
    private final Map<String, String> headers = new HashMap<>();

    public CapturingResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void addHeader(String name, String value) {
        headers.put(name, value);
        super.addHeader(name, value);
    }

    @Override
    public void setHeader(String name, String value) {
        headers.put(name, value);
        super.setHeader(name, value);
    }

    public Map<String, String> getCapturedHeaders() {
        return headers;
    }

    @Override
    public PrintWriter getWriter() {
        if (writer == null) {
            writer = new PrintWriter(byteArrayOutputStream);
        }
        return  writer;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {

        var parentStream = super.getOutputStream();
        if (outputStream == null) {
            outputStream = new ServletOutputStream() {
                @Override
                public boolean isReady() {
                    return parentStream.isReady();
                }

                @Override
                public void setWriteListener(WriteListener writeListener) {
                    parentStream.setWriteListener(writeListener);
                }

                @Override
                public void write(int b) throws IOException {
                    byteArrayOutputStream.write(b);
                    parentStream.write(b);
                }
            };
        }
        return outputStream;
    }


    public byte[] getCapturedResponseBody() {
        try {
            if (writer != null) {
                writer.flush();
            }
            if (outputStream != null) {
                outputStream.flush();
            }
        } catch (IOException e) {
            // Handle exceptions if needed
        }
        return byteArrayOutputStream.toByteArray();
    }
}