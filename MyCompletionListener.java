package asyncSender;

import java.util.concurrent.CountDownLatch;

import javax.jms.CompletionListener;
import javax.jms.Message;

public class MyCompletionListener implements CompletionListener {

//	   CountDownLatch latch;
//	   Exception exception;
	   
	   public MyCompletionListener() {
	      //this.latch=latch;
	   }

	   @Override
	   public void onCompletion(Message message) {
		   System.out.println("Message ack received"+message);
//		   try {
//	        Thread.sleep(5000);
//        } catch (InterruptedException e) {
//	        // TODO Auto-generated catch block
//	        e.printStackTrace();
//        }
//		   System.out.println("done");
	      //latch.countDown();
	   }

	   @Override
	   public void onException(Message message, Exception exception) {
//	      latch.countDown();
//	      this.exception=exception;
	   }

//	   public Exception getException(){
//	      return exception;
//	   }
	}
