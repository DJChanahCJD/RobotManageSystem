package robotManageSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 启动类，需要扫描华为 SDK 包和用户项目目录包
 *
 * @since 2024-04-10
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.huawei.innovation", "robotManageSystem"})
public class RobotManageSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(RobotManageSystemApplication.class, args);
    }
}


