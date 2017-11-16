package exceptii;

/**
 *
 * @author ccojo
 */
public class RegisterException extends RuntimeException {
    private boolean filePermission;
    
    public RegisterException(){
        filePermission = false;
    }
    
    public boolean getFilePermission(){
        return filePermission;
    }
}