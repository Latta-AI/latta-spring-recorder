package ai.latta.spring.utilities;

import ai.latta.spring.wrappers.CapturingResponseWrapper;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ScriptResponseModify {

    private static final String LATTA_SCRIPT = """
<script src="https://latta.ai/scripts/browser/latest.js"></script>
<script>
Latta.init({
    apiKey: "$API_KEY"
});
</script>""";
    private final String apiKey;

    public ScriptResponseModify(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Rewrite response if possible. Content-Type must be HTML
     * @param response Response
     * @param responseWrapper Capture wrapper
     * @throws IOException
     */
    public void rewriteResponse(HttpServletResponse response, CapturingResponseWrapper responseWrapper) throws IOException {
        var contentType = response.getContentType();

        if(contentType == null) return;
        if(!contentType.contains("text/html")) return;

        byte[] content = responseWrapper.getCapturedResponseBody();
        String modifiedContent = new String(content);

        var scriptContent = LATTA_SCRIPT.replace("$API_KEY", this.apiKey);

        // Append head if missing
        if(!modifiedContent.contains("</head>")) {
            scriptContent = "<head>" + scriptContent + "</head>";
        }

        var headInjectResult = tryToInjectTo("</head>", modifiedContent, scriptContent);

        if(headInjectResult != null) {
            modifiedContent = headInjectResult;
        }else {
            var htmlInjectResult = tryToInjectTo("</html>", modifiedContent, scriptContent);
            if(htmlInjectResult != null) { modifiedContent = htmlInjectResult; }
        }

        // Write modified content back to the actual response
        response.setContentLength(modifiedContent.length());
        response.getOutputStream().write(modifiedContent.getBytes());
        response.getOutputStream().flush();
    }

    private static @Nullable String tryToInjectTo(String tagToInjectBefore, String content, String injectContent) {
        if(!content.contains(tagToInjectBefore)) return null;

        var endIndex = content.indexOf(tagToInjectBefore);
        if(endIndex == -1) return null;

        return content.substring(0, endIndex) + injectContent + content.substring(endIndex);
    }
}
