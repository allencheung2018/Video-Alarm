package com.hri.ess.businesslogic;

import com.hri.ess.command.AnswerMsgAlarm;
import com.hri.ess.command.AnswerMsgAlarm.AlarmAnswer;
import com.hri.ess.dbservice.control.ALarmInfoControl;
import com.hri.ess.dbservice.domain.AlarmInfo;

import android.content.Context;


/**
 * 推�?报警业务逻辑处理
 * @author yu
 *
 */
public class AlarmBussinessLogic  {
	private Context mcontext;
	private ALarmInfoControl alarmRc ;
	private AlarmInfo alarmData;
	
	public AlarmBussinessLogic(Context context){
		mcontext = context;	
		alarmRc = new ALarmInfoControl(mcontext);
	}
	
	/**
	 * 添加新的报警信息
	 * @param eArgs 
//	 */
	public boolean addNewAlarm(Object eArgs){
		AnswerMsgAlarm msgAlarm = (AnswerMsgAlarm) eArgs;
		alarmData = new AlarmInfo();		
		alarmData.setAlarmId(msgAlarm.alarmId);
		alarmData.setAlarmType(msgAlarm.alarmType);
		alarmData.setAlarmChannel(msgAlarm.alarmChannel);
		alarmData.setAlarmTime(msgAlarm.alarmTime);
		if(msgAlarm.alarmType == 109 || msgAlarm.alarmType == 111){
			alarmData.setAlarmX(msgAlarm.x);
			alarmData.setAlarmY(msgAlarm.y);
		}else{		
		    alarmData.setAlarmImg(msgAlarm.alarmImg);
		}
		return alarmRc.add(alarmData);
	}
	
	public void update(AlarmInfo alarmInfo){
		alarmRc.updata(alarmInfo);
	}
	
	public void deleteAlarm(AlarmInfo alarmInfo){
		alarmRc.deleteAlarm(alarmInfo);
	}
	
	public AlarmAnswer AnswerAlaem(Object eArgs){
		AnswerMsgAlarm msgAlarm = (AnswerMsgAlarm) eArgs;
		AlarmAnswer answer = msgAlarm.MsgToAnswer();
		return answer;
	}
	

}
