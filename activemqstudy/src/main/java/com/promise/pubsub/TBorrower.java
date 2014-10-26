/** @文件名: TBorrower.java @创建人：邢健  @创建日期： 2013-12-2 上午8:11:26 */

package com.promise.pubsub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**   
 * @类名: TBorrower.java 
 * @包名: com.promise.pubsub 
 * @描述: 利用率主题的一个订阅者，是一个异步消息侦听器 
 * @作者: xingjian xingjian@yeah.net   
 * @日期:2013-12-2 上午8:11:26 
 * @版本: V1.0   
 */
public class TBorrower implements MessageListener {

	private TopicConnection tConnection;
	private TopicSession tSession;
	private Topic topic;
	private double currentRate;
	
	public TBorrower(String topiccf,String topicName,String rate) {
		try{
			currentRate = Double.valueOf(rate);
			Context ctx = new InitialContext();
			TopicConnectionFactory qFactory = (TopicConnectionFactory)ctx.lookup(topiccf);
			tConnection = qFactory.createTopicConnection();
			//创建jms会话
			tSession = tConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			topic = (Topic)ctx.lookup(topicName);
			//创建消息侦听器
			TopicSubscriber subscriber = tSession.createSubscriber(topic);
			subscriber.setMessageListener(this);
			//创建完成，启动连接
			tConnection.start();
			System.out.println("Wating for loan requests...");
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
			BytesMessage msg = (BytesMessage)arg0;
			double newRate = msg.readDouble();
			//如果该利率比当前利率至少低于一个百分点，然后推荐贷款
			if((currentRate-newRate)>1.0){
				System.out.println("New Rate ="+newRate+" - Consider refinancing loan");
			}else{
				System.out.println("New Rate ="+newRate+" - keep existing loan");
			}
			System.out.println("\nWaiting for rate updates...");
		}catch(JMSException jmse){
			jmse.printStackTrace();
			System.exit(1);
		}
	}
	
	private void exit(){
		try {
			tConnection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	public static void main(String[] args) {
		String topiccf = null;
		String topicName = null;
		String rate = null;
		if(args.length==3){
			topiccf = args[0];
			topicName = args[1];
			rate = args[2];
		}else{
			System.out.println("Invalid arguments.Should be:");
			System.out.println("java TBorrower facory topic rate");
			System.exit(0);
		}
		
		TBorrower borrower = new TBorrower(topiccf, topicName, rate);
		try{
			//持续运行，直到按下确认键为止
			BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("TBorrower application started");
			System.out.println("Press enter to quit application");
			stdin.readLine();
			borrower.exit();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}

}
