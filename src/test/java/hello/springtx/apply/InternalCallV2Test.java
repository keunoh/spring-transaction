package hello.springtx.apply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class InternalCallV2Test {

    @Autowired CallService callService;

    @Test
    void printProxy() {
        log.info("callService class={}", callService.getClass());

    }



    @Test
    void externalCallV2() {
        callService.external();
    }

    @TestConfiguration
    static class InternalCallV1TestConfig {

        @Bean
        CallService callService() {
            return new CallService(internalService());
        }

        @Bean
        InternalService internalService() {
            return new InternalService();
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    static class CallService{

        //스프링의 트랜잭션 AOP 기능은 'public' 메서드에만 트랜잭션을 적용하도록
        //기본 설정이 되어 있다.

        //내부 호출을 외부 호출로 전환하였음
        private final InternalService internalService;

        public void external() {
            log.info("call external");
            printTxInfo();
            internalService.internal();
            //매우 중요
            //메서드에서 그냥 메서드 이름을 호출하면 this. 앞에 생략된 것처럼 실행된다.
            //즉, this.internal() 이런식으로 호출이 된다는 것이다.
            //this 는 나 자신의 인스턴스를 의미한다.
            //결과적으로 내부 프록시의 인스턴스를 실행하는 것이 아닌
            //CallService 인스턴스 자체의 메서드를 실행하기 때문에
            //Transaction 적용이 안 되는 것이다.
        }



        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
        }
    }

    static class InternalService {

        @Transactional
        public void internal() {
            log.info("call internal");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
        }
    }
}
