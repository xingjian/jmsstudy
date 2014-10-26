/** @文件名: TLender.java @创建人：邢健  @创建日期： 2013-12-2 上午7:44:32 */

package com.promise.pubsub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**   
 * @类名: TLender.java 
 * @包名: com.promise.pubsub 
 * @描述: 负责像一个主题发布新的抵押利率
 * @作者: xingjian xingjian@yeah.net   
 * @日期:2013-12-2 上午7:44:32 
 * @版本: V1.0   
 */
public class TLender {

	
	private TopicConnection tConnect = null;
	private TopicSession tSession = null;
	private Topic topic = null;
	
	public TLender(String topiccf,String topicName) {
		try{
			//连接到提供者，并获取jms连接
			Context ctx = new InitialContext();
			TopicConnectionFactory qFactory = (TopicConnectionFactory)ctx.lookup(topiccf);
			tConnect = qFactory.createTopicConnection();
			//创建jms会话
			tSession = tConnect.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			//查找请求和响应队列
			topic = (Topic)ctx.lookup(topicName);
			//现在创建已经完成，启动连接
			tConnect.start();
		}catch(JMSException jmse){
			jmse.printStackTrace();
		}catch(NamingException jne){
			jne.printStackTrace();
		}
	}

	private void publishRate(double newRate){
		try{
			//创建jsm消息
			BytesMessage msg = tSession.createBytesMessage();
			msg.writeDouble(newRate);
			//创建发布者，并发布消息
			TopicPublisher publisher = tSession.createPublisher(topic);
			publisher.publish(msg);
		}catch(JMSException jmse){
			jmse.printStackTrace();
		}
	}
	
	private void exit(){
		try {
			tConnect.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public static void main(String[] args) {
		String topiccf = null;
		String topicName = null;
		if(args.length==2){
			topiccf = args[0];
			topicName = args[1];
		}else{
			System.out.println("Invalid arguments. should be:");
			System.out.println("java TLender factory topic");
			System.exit(0);
		}
		TLender lender = new TLender(topiccf,topicName);
		try{
			BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("TLender Application started");
			System.out.println("Press enter to quit application");
			System.out.println("Enter:Rate");
			System.out.println("\ne.g. 6.8");
			while(true){
				System.out.println(">");
				String rate = stdin.readLine();
				if(null == rate||rate.trim().length()<=0){
					lender.exit();
				}
				//解析交易说明
				double newRate = Double.parseDouble(rate);
				lender.publishRate(newRate);
			}
		}catch(IOException ioe){
			ioe.printStackTrace();
			
		}
	}

}
