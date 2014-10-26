/** @文件名: Chat.java @创建人：邢健  @创建日期： 2013-11-20 上午7:28:05 */

package com.promise.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;

/**   
 * @类名: Chat.java 
 * @包名: com.promise.chat 
 * @描述: 聊天程序
 * @作者: xingjian xingjian@yeah.net   
 * @日期:2013-11-20 上午7:28:05 
 * @版本: V1.0   
 */
public class Chat implements javax.jms.MessageListener{

	private TopicSession pubSession;
	private TopicPublisher publisher;
	private TopicConnection connection;
	private String username;
	
	public Chat(String topicFactory,String topicName,String username)throws Exception{
		//使用jndi.properties文件获取一个jndi连接
		InitialContext ctx = new InitialContext();
		//查找一个jms连接工厂,并创建连接
		TopicConnectionFactory conFactory = (TopicConnectionFactory)ctx.lookup(topicFactory);
		TopicConnection connection = conFactory.createTopicConnection();
		//创建两个jms会话对象
		TopicSession pubSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		TopicSession subSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		//查找一个jms主题
		Topic chatTopic = (Topic)ctx.lookup(topicName);
		//创建一个jms发布者和订阅者。createSubscriber中附加的参数是一个消息
		//选择器(null)和nolocal标记的一个真值，它表名这个发布者生产的小时不被它自己所消费
		TopicPublisher pubLisher = pubSession.createPublisher(chatTopic);
		TopicSubscriber subscriber = subSession.createSubscriber(chatTopic,null,false);
		//设置一个jms消息侦听器
		subscriber.setMessageListener(this);
		//初始化Chat应用程序变量
		this.connection = connection;
		this.pubSession = pubSession;
		this.publisher = pubLisher;
		this.username = username;
		//启动jms连接，允许传送消息
		connection.start();
	}
	
	/**
	 * 接收来自topicsubscriber的消息
	 */
	@Override
	public void onMessage(Message message) {
		try {
			TextMessage textMessage = (TextMessage)message;
			System.out.println(textMessage.getText());
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 使用发布者创建并发送消息
	 * @param text
	 * @throws JMSException
	 */
	protected void writeMessage(String text) throws JMSException{
		TextMessage message = pubSession.createTextMessage();
		message.setText(text);
		publisher.publish(message);
	}

	/**
	 * 关闭jms连接
	 * @throws JMSException
	 */
	public void close() throws JMSException{
		connection.close();
	}
	
	/**
	 * 运行聊天客户端
	 */
	public static void main(String[] args) {
		try{
			if(args.length!=3){
				System.out.println("Factory ,Topic,or username missing");
			}
			//args[0] = topicFactory; args[1] = topicName; args[2] = username;
			Chat chat = new Chat(args[0],args[1],args[2]);
			//从命令行读取
			BufferedReader commandLine = new BufferedReader(new InputStreamReader(System.in));
			//循环,直至键入exit为止
			while(true){
				String s = commandLine.readLine();
				if(s.equalsIgnoreCase("exit")){
					chat.close();
					System.exit(0);
				}else{
					chat.writeMessage(s);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}

	}

}
