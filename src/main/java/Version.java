import zx.zxlive.core.server.api.Red5;

/**
 * Provides information about the version of Red5 being used.
 * 
 * @author Vic Wang (xiaoyu860912@163.com)
 */
public class Version {

    public static void main(String[] args) {
        System.out.printf("Red5 version: %s %s%n", Red5.getVersion(), Red5.getFMSVersion());
    }

}
