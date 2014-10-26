/** @文件名: QLender.java @创建人：邢健  @创建日期： 2013-11-27 上午8:07:20 */

package com.promise.p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**   
 * @类名: QLender.java 
 * @包名: com.promise.p2p 
 * @描述: 监听贷款申请队列上的贷款申请，判断工资额是否满足必要的商业要求
 *        并最终将结果发回给借方 
 * @作者: xingjian xingjian@yeah.net   
 * @日期:2013-11-27 上午8:07:20 
 * @版本: V1.0   
 */
public class QLender implements MessageListener {

	private QueueConnection qConnect = null;
	private QueueSession qSession = null;
	private Queue requestQ = null;
	
	public QLender(String queuecf,String requestQueue){
		try{
			//连接到提供者并获得连接
			Context ctx = new InitialContext();
			QueueConnectionFactory qcf = (QueueConnectionFactory)ctx.lookup(queuecf);
			qConnect = qcf.createQueueConnection();
			//创建jsm会话
			qSession = qConnect.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			//查找申请队列
			requestQ = (Queue)ctx.lookup(requestQueue);
			//创建完成，启动连接
			qConnect.start();
			//创建消息侦听器
			QueueReceiver qReceiver = qSession.createReceiver(requestQ);
			qReceiver.setMessageListener(this);
			System.out.println("Waiting for loan requests......");
		}catch(JMSException jmse){
			jmse.printStackTrace();
			System.exit(1);
		}catch(NamingException jne){
			jne.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	public void onMessage(Message arg0) {
		try{
			boolean accepted = false;
			//从消息中获取数据
			MapMessage msg = (MapMessage)arg0;
			double salary = msg.getDouble("Salary");
			double loanAmt = msg.getDouble("LoanAmount");
			//决定是否接受或拒绝贷款申请
			if(loanAmt < 200000){
				accepted = (salary/loanAmt)>0.25;
			}else{
				accepted = (salary/loanAmt)>0.33;
			}
			System.out.println(""+"%="+(salary/loanAmt)+",loan is"+(accepted?"accepted":"declined"));
			//将结果发送回借方
			TextMessage tmsg = qSession.createTextMessage();
			tmsg.setText(accepted?"accepted":"declined");
			tmsg.setJMSCorrelationID(arg0.getJMSMessageID());
			//创建发送者并发送消息
			QueueSender qSender = qSession.createSender((Queue)arg0.getJMSReplyTo());
			qSender.send(tmsg);
			System.out.println("\nWaiting for loan requests......");
		}catch(JMSException jmse){
			jmse.printStackTrace();
			System.exit(1);
		}
	}

	private void exit(){
		try{
			qConnect.close();
		}catch(JMSException jmse){
			jmse.printStackTrace();
		}
		System.exit(0);
	}
	
	public static void main(String[] args) {
		String queuecf = null;
		String requestq = null;
		if(args.length==2){
			queuecf = args[0];
			requestq = args[1];
		}else{
			System.out.println("Invalid arguments. Should be:");
			System.out.println("java QLender factory request_queue");
			System.exit(0);
		}
		QLender lender = new QLender(queuecf,requestq);
		try{
			//持续运行直到按下确定为止
			BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("QLender application started");
			System.out.println("Press enter to quit application");
			stdin.readLine();
			lender.exit();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
}
