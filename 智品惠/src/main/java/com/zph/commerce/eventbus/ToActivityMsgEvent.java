package com.zph.commerce.eventbus;

import com.zph.commerce.bean.Campaign;

/**

 */
public class ToActivityMsgEvent {
	private Campaign campaign;

	public ToActivityMsgEvent() {
		super();
	}

	public Campaign getCampaign() {
		return campaign;
	}

	public ToActivityMsgEvent(Campaign campaign) {
		this.campaign = campaign;
	}
}
