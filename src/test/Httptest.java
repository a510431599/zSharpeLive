import com.wh.movie.Main;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by WH-2013 on 2017/4/1.
 */
public class Httptest {
    @Test
    public void test() throws IOException {
        Main main = new Main();
        String s = main.doGet("https://movie.douban.com/subject/25934014/");
        main.parse(s);
    }
}
