package com.stercomm.customers.rbs.sir.rest.util;

import com.stercomm.customers.rbs.sir.rest.domain.RoutingRule;
import com.stercomm.customers.rbs.sir.rest.domain.SWIFTRoutingRule;
import com.sterlingcommerce.woodstock.ui.SWIFTNetRoutingRuleObj;

public class SSROBuilderTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RoutingRule r = new RoutingRule();
		r.setActionType("BP");
		r.setPriority(1);
		r.setInvokeMode("SYNC");
		r.setRequestorDN("");
		r.setResponderDN("");
		r.setRequestType(new String[]{"MT202.PM"});
		r.setEntityName("");
		r.setService("");
		r.setUsername("ASmith");

		SWIFTNetRoutingRuleObj swiftRule = new SWIFTRoutingRule.Builder()
				.withActionType(r.getActionType())
				.withPriority(r.getPriority())
				.withService(r.getService())
				.withInvokeMode(r.getInvokeMode())
				.withRequestType(r.getRequestType()[0])
				.withRequestorDN(r.getRequestorDN())
				.withResponderDN(r.getResponderDN())
				.withWorkflowName(r.getRequestType()[0])
				.toRouteName(r.getEntityName(), r.getRequestType()[0])
				.build();
	
	}

}
