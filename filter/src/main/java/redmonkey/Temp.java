package redmonkey;

/**
 * Created by urwisy on 2015-03-20.
 */
public class Temp {
   /* public static void inte(){
        // use the web applications temporary work directory
        File tempDir =
                (File)sc.getAttribute("javax.servlet.context.tempdir");

// look to see if a cached copy of the response exists
        String temp = tempDir.getAbsolutePath();
        File file = new File(temp+id);

// get a reference to the servlet/JSP
// responsible for this cache
        if (path == null) {
            path = sc.getRealPath(request.getRequestURI());
        }
        File current = new File(path);

// check if the cache exists and is newer than the
// servlet or JSP responsible for making it.
        try {
            long now = Calendar.getInstance().getTimeInMillis();
            //set timestamp check
            if (!file.exists() || (file.exists() &&
                    current.lastModified() > file.lastModified()) ||
                    cacheTimeout < now - file.lastModified()) {

                // if not, invoke chain.doFilter() and
                // cache the response
                String name = file.getAbsolutePath();
                name = name.substring(0,name.lastIndexOf("/"));
                new File(name).mkdirs();
                ByteArrayOutputStream baos =
                        new ByteArrayOutputStream();
                CacheResponseWrapper wrappedResponse =
                        new CacheResponseWrapper(response, baos);
                chain.doFilter(req, wrappedResponse);

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
            }
        } catch (ServletException e) {
            if (!file.exists()) {
                throw new ServletException(e);
            }
        }
        catch (IOException e) {
            if (!file.exists()) {
                throw e;
            }
        }

// return to the client the cached resource.
        FileInputStream fis = new FileInputStream(file);
        String mt = sc.getMimeType(request.getRequestURI());
        response.setContentType(mt);
        ServletOutputStream sos = res.getOutputStream();
        for (int i = fis.read(); i!= -1; i = fis.read()) {
            sos.write((byte)i);
        }
    }*/
}
