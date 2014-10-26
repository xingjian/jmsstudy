/** @文件名: QBorrower.java @创建人：邢健  @创建日期： 2013-11-27 上午7:22:06 */

package com.promise.p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import javax.jms.JMSException;
import javax.jms.MapMessage;
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
 * @类名: QBorrower.java 
 * @包名: com.promise.p2p 
 * @描述: 负责向包含工资额和贷款额的一个队列发送LoanRequest消息。
 * @作者: xingjian xingjian@yeah.net   
 * @日期:2013-11-27 上午7:22:06 
 * @版本: V1.0   
 */
public class QBorrower {

	private QueueConnection qConect = null;
	private QueueSession qSession = null;
	private Queue responseQ = null;
	private Queue requestQ  = null;
	
	public QBorrower(String queuecf,String requestQueue,String responseQueue){
		try{
			//创建提供者并提供jms连接
			Context ctx = new InitialContext();
			QueueConnectionFactory qcf = (QueueConnectionFactory)ctx.lookup(queuecf);
			qConect = qcf.createQueueConnection();
			//创建jms会话
			qSession = qConect.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			//创建请求和响应队列
			responseQ = (Queue)ctx.lookup(responseQueue);
			requestQ = (Queue)ctx.lookup(requestQueue);
			//现在完成创建，启动连接
			qConect.start();
			
		}catch(JMSException jmse){
			jmse.printStackTrace();
			System.exit(1);
		}catch(NamingException jne){
			jne.printStackTrace();
			System.exit(1);
		}
	}
	
	private void sendLoanRequest(double salary,double loanAmt){
		try{
			//创建jms消息
			MapMessage msg = qSession.createMapMessage();
			msg.setDouble("Salary", salary);
			msg.setDouble("LoanAmount", loanAmt);
			msg.setJMSReplyTo(responseQ);
			//创建发送者并发送消息
			QueueSender qSender = qSession.createSender(requestQ);
			qSender.send(msg);
			//等待查看贷款申请被接收或者拒绝
			String filter = "JMSCorrelationID = '"+msg.getJMSMessageID()+"'";
			QueueReceiver qReceiver = qSession.createReceiver(responseQ,filter);
			TextMessage tmsg = (TextMessage)qReceiver.receive(30000);
			if(null == tmsg){
				System.out.println("QLender not responding");
			}else{
				System.out.println("Loan request was "+tmsg.getText());
			}
		}catch(JMSException jmse){
			jmse.printStackTrace();
			System.exit(1);
		}
	}
	
	private void exit(){
		try{
			qConect.close();
		}catch(JMSException jmse){
			jmse.printStackTrace();
		}
		System.exit(0);
	}
	
	public static void main(String[] args) {
		String queuecf = null;
		String requestq = null;
		String responseq = null;
		if(args.length==3){
			queuecf = args[0];
			requestq = args[1];
			responseq = args[2];
		}else{
			System.out.println("Invalid arguments...... should be:");
			System.exit(0);
		}
		
		QBorrower borrower = new QBorrower(queuecf,requestq,responseq);
		try{
			//读取所有的标准输入，并将它作为一条信息发送
			BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("QBorrower Application Started");
			System.out.println("Press enter to quit application");
			System.out.println("Enter:Salary,Loan_Amount");
			System.out.println("\ne.g 50000,120000");
			while(true){
				System.out.println(">");
				String loanRequest = stdin.readLine();
				if(null==loanRequest||loanRequest.trim().length()<=0){
					borrower.exit();
				}
				
				//解析交易说明
				StringTokenizer st = new StringTokenizer(loanRequest, ",");
				double salary = Double.valueOf(st.nextToken().trim()).doubleValue();
				double loanAmt = Double.valueOf(st.nextToken().trim()).doubleValue();
				borrower.sendLoanRequest(salary, loanAmt);
			}
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
}
