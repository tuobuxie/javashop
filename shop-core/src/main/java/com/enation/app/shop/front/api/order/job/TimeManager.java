package com.enation.app.shop.front.api.order.job;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.enation.app.shop.front.api.order.PaymentApiController;

public class TimeManager  implements ServletContextListener{

	//設置定時器
	public synchronized static void TimeStart(){
		Runnable runnable = new Runnable() {
			public void run() {
				System.out.println("***");//填要執行的方法
				queryPay();
				
			}
		};
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
//		設置執行間隔  Runnable command, long initialDelay, long period, TimeUnit unit
		service.scheduleAtFixedRate(runnable, 0, 10, TimeUnit.SECONDS);
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	}
	//在項目執行時調用
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		TimeStart();//啓動項目是進行調用
	}
	
	public static void queryPay() {
		PaymentApiController job = new PaymentApiController();
		//job.getH5PayResultJob();
		job.getSqQrPayResultJob();
		
		
	}
}
