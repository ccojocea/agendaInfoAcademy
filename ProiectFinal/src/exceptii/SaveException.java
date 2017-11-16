package exceptii;

/**
 *
 * @author ccojo
 */
public class SaveException extends RuntimeException {
    private boolean filePermission;
    
    public SaveException(){
        filePermission = false;
    }
    
    public boolean getFilePermission(){
        return filePermission;
    }
}