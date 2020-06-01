package com.stercomm.customers.rbs.sir.rest.util;

import com.stercomm.customers.rbs.sir.rest.domain.RoutingRule;
import com.stercomm.customers.rbs.sir.rest.domain.SWIFTRoutingRule;
import com.sterlingcommerce.woodstock.ui.SWIFTNetRoutingRuleObj;

public class SSROBuilderTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RoutingRule rule = new RoutingRule();
		
		rule.setRequestorDN("");
		rule.setResponderDN("");
		rule.setRequestType(new String[]{"MT202.PM"});
		rule.setEntityName("");
		rule.setService("");
		rule.setUsername("ASmith");

		SWIFTNetRoutingRuleObj swiftRule = new SWIFTRoutingRule.Builder()
				.withActionType("BP")
				.withService(rule.getService())
				.withInvokeMode("SYNC")
				.withRequestType(rule.getRequestType()[0])
				.withRequestorDN(rule.getRequestorDN())
				.withResponderDN(rule.getResponderDN())
				.withWorkflowName(rule.getRequestType()[0])
				.toRouteName(rule.getEntityName(), rule.getRequestType()[0])
				.build();
		System.out.println(swiftRule);
	}

}
