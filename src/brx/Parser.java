package brx;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Parser {

	private Integer storeNo;
	private Integer custNo;
	private String countryCd;
	private String countryNb;
	private String taxNo;
	private String comment;
	
	private Element ccdElm;
	private List<Element> dynInd;	
	private final String SC_SELL_VAL_NSP_L6M = "SC_SELL_VAL_NSP_L6M";
	private enum BehvIndOld { BEHV_SELL_VAL_NSP_FORECAST, BEHV_SELL_VAL_NSP_L12M, BEHV_SELL_VAL_NSP_LYTD, BEHV_SELL_VAL_NSP_YTD };
	private enum BehvIndNew { BEHV_SELL_VAL_NSP_FORECAST, BEHV_SELL_VAL_NSP_L12M, BEHV_SELL_VAL_NSP_L1M, BEHV_SELL_VAL_NSP_L3M, BEHV_SELL_VAL_NSP_L6M, BEHV_SELL_VAL_NSP_LYTM, BEHV_SELL_VAL_NSP_YTM };
	private final String BEHV_PREFIX = "BEHV";
	private final String TAB = "\t";

	public Parser(Document xml, Integer storeNo, Integer custNo, String countryCd, String countryNb, String taxNo, String comment) {
		
		//Set attributes
		this.storeNo	= storeNo; 
		this.custNo  	= custNo;  
		this.countryCd	= countryCd;
		this.countryNb	= countryNb;
		this.taxNo		= taxNo;
		this.comment	= comment;
		
		//Get <this:customerCreditData> element
		NodeList nlccd = xml.getElementsByTagName("this:customerCreditData");
		if (nlccd!=null && nlccd.getLength()==1) {
			ccdElm = (Element) nlccd.item(0);
		}
		else
			throw new IllegalStateException("XML is not valid! No CustomerCreditData found!");
		
		//Get dynamic indicator elements <dynamicIndicators indicator="" key="" value=""/>
		dynInd = new ArrayList<Element>();
		NodeList nlDi = xml.getElementsByTagName("dynamicIndicators");
		if (nlDi!=null) {
			for (int i=0; i<nlDi.getLength(); i++) {				
				dynInd.add((Element) nlDi.item(i));
			}
		}
		else
			throw new IllegalStateException("XML is not valid! No dynamic indicators found!");	

	}
	
	
	/**
	 * Generates an SQL command for inserting data for Customer Credit Data in the new MDW inteface
	 * 
	 * @return SQL commands
	 */
	protected String generateSql4MdwNew() {	
	
		StringBuilder sb = new StringBuilder();

		sb.append("-- "+storeNo+"/"+custNo+"\n\n");
		
		//insert into DW_V_CCAM_CUST 
		sb.append("INSERT INTO DW_V_CCAM_CUST " +
					"( CLIENT_CD, CUST_NO, DESCRIPTION, FAX_NO_IND, HOME_STORE_ID, " +
					"LEGAL_CASE_IND, LEGAL_DURATION_TIME, METROPLUS_RELATION, MONITION_LEVEL_MAX_L12M, MONTH_ID, " +					
					"NUM_DEBIT_ENTRIES_L12M, NUM_INVOICES_L12M, NUM_INVOICES_L1M, NUM_INVOICES_L3M, NUM_INVOICES_L6M, " +					
					"NUM_INVOICES_LYTM, NUM_INVOICES_YTM, NUM_PURCHASES_F_STORE_L12M, NUM_PURCHASES_F_STORE_L1M, NUM_PURCHASES_F_STORE_L3M, " +					
					"NUM_PURCHASES_F_STORE_L6M, NUM_PURCHASES_L12M, NUM_PURCHASES_L1M, NUM_PURCHASES_L3M, NUM_PURCHASES_L6M, " +					
					"SELL_VAL_DFD_MAX_1_L12M, SELL_VAL_DFD_MAX_2_L12M, SELL_VAL_DF_MAX_L12M, SELL_VAL_NNBP_L12M, SELL_VAL_NSP_F_L12M, " +										
					"SELL_VAL_NSP_L12M, SELL_VAL_NSP_L12M_F_STORE, SELL_VAL_NSP_L12M_H_STORE, SELL_VAL_NSP_L1M, SELL_VAL_NSP_L24M, " +										
					"SELL_VAL_NSP_L3M, SELL_VAL_NSP_L6M, SELL_VAL_NSP_LYL3M, SELL_VAL_NSP_LYTM, SELL_VAL_NSP_NF_L12M, " +
					"SELL_VAL_NSP_YTM, TIMEZONE_CD) " +
				   "VALUES");
	
		sb.append("("+countryNb+", "+custNo+", '"+comment+"', "+ccdElm.getAttribute("faxNoInd")+", "+storeNo+", " +
				  ""+ccdElm.getAttribute("legalCaseInd")+", "+ccdElm.getAttribute("legalDurationTime")+", "+ccdElm.getAttribute("metroplusRelation")+", "+ccdElm.getAttribute("monitionLevelMaxL12M")+", "+ccdElm.getAttribute("monthID")+", " +				  			  
				  ""+ccdElm.getAttribute("numDebitEntriesL12M")+", 0, "+ccdElm.getAttribute("numInvoicesL1M")+", "+ccdElm.getAttribute("numInvoicesL3M")+", "+ccdElm.getAttribute("numInvoicesL6M")+", " +				  
				  ""+ccdElm.getAttribute("numInvoicesLy")+", "+ccdElm.getAttribute("numInvoicesYtd")+", "+ccdElm.getAttribute("numPurchasesL12M")+", "+null2zero(ccdElm.getAttribute("numPurchasesL1M"))+", "+ccdElm.getAttribute("numPurchasesL3M")+", " +				  						  
				  ""+ccdElm.getAttribute("numPurchasesStoreL6M")+", "+ccdElm.getAttribute("numPurchasesL12M")+", "+null2zero(ccdElm.getAttribute("numPurchasesL1M"))+", "+ccdElm.getAttribute("numPurchasesL3M")+", "+ccdElm.getAttribute("numPurchasesL6M")+", " +				  
				  ""+ccdElm.getAttribute("sellValDfdMax1L12M")+", "+ccdElm.getAttribute("sellValDfdMax2L12M")+", 0, 0, 0, " +				  
				  ""+ccdElm.getAttribute("sellValNspL12M")+", 0, 0, "+ccdElm.getAttribute("sellValNspL1M")+", 0, " +				  
				  ""+ccdElm.getAttribute("sellValNspL3M")+", "+ccdElm.getAttribute("sellValNspL6M")+", "+ccdElm.getAttribute("sellValNspLYL3M")+", "+ccdElm.getAttribute("sellValNspLytd")+", 0, " +
				  ""+ccdElm.getAttribute("sellValNspYtd")+", "+ccdElm.getAttribute("timezoneCD")+");");
		
		sb.append("\n\n");		

		//insert into DW_V_CCAM_BEHAVIOUR 
		HashMap<String, HashMap<BehvIndNew, String>> behvMap = new HashMap<String, HashMap<BehvIndNew,String>>(); //Key -> Indicator-Value
		for (Element dynElm : dynInd) {			
			if (dynElm.getAttribute("indicator").toUpperCase().startsWith(BEHV_PREFIX))
			{
				String key = dynElm.getAttribute("key");
				if (behvMap.get(key)==null) //If the entry does not exist then create it 
				{
					HashMap<BehvIndNew, String> val = new HashMap<BehvIndNew, String>();
					val.put(BehvIndNew.valueOf(dynElm.getAttribute("indicator").toUpperCase()), dynElm.getAttribute("value"));
					behvMap.put(key, val);
				}
				else //If the entry already exists add the values to the map
				{
					HashMap<BehvIndNew, String> val = (HashMap<BehvIndNew, String>)behvMap.get(key);
					val.put(BehvIndNew.valueOf(dynElm.getAttribute("indicator").toUpperCase()), dynElm.getAttribute("value"));
				}
				
			}
			//Calculate percentages
			else if  (dynElm.getAttribute("indicator").equalsIgnoreCase(SC_SELL_VAL_NSP_L6M))
			{
				if (ccdElm.getAttribute("sellValNspL6M")==null || ccdElm.getAttribute("sellValNspL6M").equals("0"))
					throw new IllegalArgumentException("sellValNspL6M cannot be null or zero for percentage dynamic indicators");
				
				Long svnl6m = Long.valueOf(ccdElm.getAttribute("sellValNspL6M"));
				Long perc   = Long.valueOf(dynElm.getAttribute("value"));
				String key  = dynElm.getAttribute("key");
				Long bhvl6m = (perc*svnl6m)/100;
				
				HashMap<BehvIndNew, String> val = new HashMap<BehvIndNew, String>();
				val.put(BehvIndNew.BEHV_SELL_VAL_NSP_L6M, bhvl6m.toString());
				behvMap.put(key, val);
			}
		}
		
		for (Entry<String, HashMap<BehvIndNew,String>> entry : behvMap.entrySet()) {
			
			sb.append("INSERT INTO DW_V_CCAM_BEHAVIOUR " +
			"(CLIENT_CD,CUST_NO,HOME_STORE_ID,CCA_BEHAVIOURAL_CD,BEHV_SELL_VAL_NSP_L12M,BEHV_SELL_VAL_NSP_YTM,BEHV_SELL_VAL_NSP_LYTM,BEHV_SELL_VAL_NSP_FORECAST,DESCRIPTION,BEHV_SELL_VAL_NSP_L1M,BEHV_SELL_VAL_NSP_L3M,BEHV_SELL_VAL_NSP_L6M) " +
			"VALUES");	
			
			String l12m = entry.getValue().get(BehvIndNew.BEHV_SELL_VAL_NSP_L12M)    ==null?"0":entry.getValue().get(BehvIndNew.BEHV_SELL_VAL_NSP_L12M);
			String fcst = entry.getValue().get(BehvIndNew.BEHV_SELL_VAL_NSP_FORECAST)==null?"0":entry.getValue().get(BehvIndNew.BEHV_SELL_VAL_NSP_FORECAST);
			String l1m  = entry.getValue().get(BehvIndNew.BEHV_SELL_VAL_NSP_L1M)     ==null?"0":entry.getValue().get(BehvIndNew.BEHV_SELL_VAL_NSP_L1M);
			String l3m  = entry.getValue().get(BehvIndNew.BEHV_SELL_VAL_NSP_L3M)     ==null?"0":entry.getValue().get(BehvIndNew.BEHV_SELL_VAL_NSP_L3M);
			String l6m  = entry.getValue().get(BehvIndNew.BEHV_SELL_VAL_NSP_L6M)     ==null?"0":entry.getValue().get(BehvIndNew.BEHV_SELL_VAL_NSP_L6M);
			String lytm = entry.getValue().get(BehvIndNew.BEHV_SELL_VAL_NSP_LYTM)    ==null?"0":entry.getValue().get(BehvIndNew.BEHV_SELL_VAL_NSP_LYTM);
			String ytm  = entry.getValue().get(BehvIndNew.BEHV_SELL_VAL_NSP_YTM)     ==null?"0":entry.getValue().get(BehvIndNew.BEHV_SELL_VAL_NSP_YTM);

			sb.append("("+countryNb+", "+custNo+", "+storeNo+", "+entry.getKey()+", "+l12m+", "+ytm+", "+lytm+", "+fcst+", '"+comment+"', "+l1m+", "+l3m+", "+l6m+");");
			
			sb.append("\n");
		}
		
		sb.append("\n");
		
		return sb.toString();
	}
	
	/**
	 * Generates an SQL command for inserting data for Customer Credit Data in the old MDW inteface
	 * 
	 * @return SQL commands
	 * @deprecated
	 */
	protected String generateSql4MdwOld() {
	
		StringBuilder sb = new StringBuilder();
		
		//insert into DW_V_CCAM_CUST 
		sb.append("INSERT INTO DW_V_CCAM_CUST " +
					"( area_cd, behv_sell_val_nsp_l3m, cca_scorecard_cd_max_l12m, " +
					"ccam_sell_val_nsp, client_cd, cust_no, fax_no_ind, home_store_id, legal_case_ind, " +
					"legal_duration_time, metroplus_relation, monition_level_max_l12m," +
					"month_id, num_debit_entries_l12m, num_invoices_l3m, " +					
					"num_invoices_ly, num_invoices_ytd, num_purchases_l12m, num_purchases_l3m, " +
					"num_purchases_store_l6m, phone_no, sc_sell_val_nsp_l3m, " +					
					"sell_val_dfd_max_1_l12m, sell_val_dfd_max_2_l12m, sell_val_nsp_avg_l6m, " +					
					"sell_val_nsp_forecast, sell_val_nsp_forecast_store, sell_val_nsp_l12m, " +					
					"sell_val_nsp_l6m, sell_val_nsp_lytd, sell_val_nsp_trend_l6m_6m, sell_val_nsp_ytd," +
					"timezone_cd, top_develop_class_cd, trend_sales_l12m_12m_back, " +
					"trend_sales_y_ly_back, description) " +
				   "VALUES");
		
		sb.append("('"+ccdElm.getAttribute("areaCD")+"', "+ccdElm.getAttribute("sellValNspLYL3M")+", "+ccdElm.getAttribute("ccaScorecardCDMaxL12M")+", " +
				  ""+ccdElm.getAttribute("sellValNspL1M")+", 12345, "+custNo+", "+ccdElm.getAttribute("faxNoInd")+", "+storeNo+", "+ccdElm.getAttribute("legalCaseInd")+", " +
				  ""+ccdElm.getAttribute("legalDurationTime")+", "+ccdElm.getAttribute("metroplusRelation")+", "+ccdElm.getAttribute("monitionLevelMaxL12M")+", " +
				  ""+ccdElm.getAttribute("monthID")+", "+ccdElm.getAttribute("numDebitEntriesL12M")+", "+ccdElm.getAttribute("numInvoicesL3M")+", " +
				  ""+ccdElm.getAttribute("numInvoicesLy")+", "+ccdElm.getAttribute("numInvoicesYtd")+", "+ccdElm.getAttribute("numPurchasesL12M")+", "+ccdElm.getAttribute("numPurchasesL3M")+", " +				  		
				  ""+ccdElm.getAttribute("numPurchasesStoreL6M")+", '"+ccdElm.getAttribute("phoneNo")+"', "+ccdElm.getAttribute("sellValNspL3M")+", " +
				  ""+ccdElm.getAttribute("sellValDfdMax1L12M")+", "+ccdElm.getAttribute("sellValDfdMax2L12M")+", "+ccdElm.getAttribute("sellValNspAvgL6M")+", " +
				  ""+ccdElm.getAttribute("sellValNspForecast")+", "+ccdElm.getAttribute("sellValNspForecastStore")+", "+ccdElm.getAttribute("sellValNspL12M")+", " +
				  ""+ccdElm.getAttribute("sellValNspL6M")+", "+ccdElm.getAttribute("sellValNspLytd")+", "+ccdElm.getAttribute("sellValNspTrendL6M6M")+", "+ccdElm.getAttribute("sellValNspYtd")+", " +
				  ""+ccdElm.getAttribute("timezoneCD")+", "+ccdElm.getAttribute("topDevelopClassCD")+", "+ccdElm.getAttribute("trendSalesL12M12MBack")+", " +
				  ""+ccdElm.getAttribute("trendSalesYLYBack")+", '"+comment+"');");
		
		sb.append("\n\n");
		
		//insert into DW_V_CCAM_BEHAVIOUR 
		HashMap<String, HashMap<BehvIndOld, String>> behvMap = new HashMap<String, HashMap<BehvIndOld,String>>(); //Key -> Indicator-Value
		for (Element dynElm : dynInd) {			
			if (dynElm.getAttribute("indicator").toUpperCase().startsWith(BEHV_PREFIX))
			{
				String key = dynElm.getAttribute("key");
				if (behvMap.get(key)==null) //If the entry does not exist then create it 
				{
					HashMap<BehvIndOld, String> val = new HashMap<BehvIndOld, String>();
					val.put(BehvIndOld.valueOf(dynElm.getAttribute("indicator").toUpperCase()), dynElm.getAttribute("value"));
					behvMap.put(key, val);
				}
				else //If the entry already exists add the values to the map
				{
					HashMap<BehvIndOld, String> val = (HashMap<BehvIndOld, String>)behvMap.get(key);
					val.put(BehvIndOld.valueOf(dynElm.getAttribute("indicator").toUpperCase()), dynElm.getAttribute("value"));
				}
				
			}			
		}
		
		for (Entry<String, HashMap<BehvIndOld,String>> entry : behvMap.entrySet()) {
			
			sb.append("INSERT INTO DW_V_CCAM_BEHAVIOUR " +
			"(client_cd, cust_no, home_store_id, cca_behavioural_cd, behv_sell_val_nsp_l12m, behv_sell_val_nsp_ytd, behv_sell_val_nsp_lytd, behv_sell_val_nsp_forecast, description) " +
			"VALUES");	
			
			String l12m = entry.getValue().get(BehvIndOld.BEHV_SELL_VAL_NSP_L12M)==null?"0":entry.getValue().get(BehvIndOld.BEHV_SELL_VAL_NSP_L12M);
			String ytd 	= entry.getValue().get(BehvIndOld.BEHV_SELL_VAL_NSP_YTD) ==null?"0":entry.getValue().get(BehvIndOld.BEHV_SELL_VAL_NSP_YTD);
			String lytd = entry.getValue().get(BehvIndOld.BEHV_SELL_VAL_NSP_LYTD)==null?"0":entry.getValue().get(BehvIndOld.BEHV_SELL_VAL_NSP_LYTD);
			String fcst = entry.getValue().get(BehvIndOld.BEHV_SELL_VAL_NSP_FORECAST)==null?"0":entry.getValue().get(BehvIndOld.BEHV_SELL_VAL_NSP_FORECAST);
			
			sb.append("(12345, "+custNo+", "+storeNo+", "+entry.getKey()+", "+l12m+", "+ytd+", "+lytd+", "+fcst+", '"+comment+"');");
			
			sb.append("\n");
		}
		
		sb.append("\n");
		
		//insert into DW_V_CCAM_SCORECARD
		for (Element dynElm : dynInd) {
			if (dynElm.getAttribute("indicator").equalsIgnoreCase(SC_SELL_VAL_NSP_L6M))
			{
				sb.append("INSERT INTO DW_V_CCAM_SCORECARD " +
						"(client_cd, cust_no, home_store_id, cca_scorecard_cd, sc_sell_val_nsp_l6m, description) " +
						"VALUES");
				
				sb.append("(12345, "+custNo+", "+storeNo+", "+dynElm.getAttribute("key")+", "+dynElm.getAttribute("value")+", '"+comment+"');");
				
				sb.append("\n");
			}
		}
			
		return sb.toString();
	}
	
	/**
	 * Generates a tab separated value String which will be pasted in the XLS simulator for MSB
	 * 
	 * @return store_key	cust_no	country_cd	owner_last_name	owner_first_name	owner_birth_date	cust_first_name	cust_last_name	zip_cd	town	street	house_no	
	 * legal_form_cd	foundation_date	spec_vat_no	eu_vat_no	payment_allowance_limit	credit_settle_type_cd	credit_settle_period_cd	credit_settle_frequency_cd	
	 * payment_allowance_cd	credit_limit_exhaustion	checkout_check_cd	blocking_reason_cd	branch_id	branch_family_id	registration_date	comment_for_cust	
	 * trading_partner_id	type_cd	metro_club_type_id	account_holder	address_type_cd	bank_account_no	bank_identifier_cd	bank_name	bank_routing_cd	
	 * bank_routing_supplement	commercial_name_1	commercial_name_2	control_id	iban	limit_account_ind	mobile_phone_no	namepart2	numberofcards	
	 * owner_salutation_cd	parent_store_key	parent_cust_no	phone_no	limit_type_indicator	invoice_type	title_name
	 */
	protected String generateRow4Simulator() {
		
		StringBuilder sb = new StringBuilder();		
		DateFormat df = new SimpleDateFormat("ddMMyyyy");

		//Calculate regDate(Date) from custRel(nbMonths)
		Integer custRel = Integer.valueOf(ccdElm.getAttribute("custRelation"));
		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date()); //current date
		cal.add(Calendar.MONTH, -custRel); //subtract number of months		
		Date regDate = cal.getTime();
		
		sb.append(storeNo)							                .append(TAB);	//store_key				
		sb.append(custNo)							                .append(TAB);	//cust_no
		sb.append(countryCd)						                .append(TAB);	//country_cd
		sb.append(countryCd+"_Lastname"+custNo)		                .append(TAB);	//owner_last_name
		sb.append(countryCd+"_Firstname"+custNo)	                .append(TAB);	//owner_first_name
		sb.append("14041944")						                .append(TAB);	//owner_birth_date
		sb.append(countryCd+"_Firstname"+custNo)	                .append(TAB);	//cust_last_name	
		sb.append(countryCd+"_Lastname"+custNo)		                .append(TAB);	//cust_first_name
		sb.append("123")							                .append(TAB);	//zip_cd
		sb.append(countryCd+"_Town")				                .append(TAB);	//town
		sb.append(countryCd+"_Street")				                .append(TAB);	//street
		sb.append("1")								                .append(TAB);	//house_no
		sb.append(ccdElm.getAttribute("legalFormCD"))               .append(TAB);	//legal_form_cd
		sb.append("06062004")						                .append(TAB);	//foundation_date
		sb.append(taxNo)							                .append(TAB);	//spec_vat_no
		sb.append(taxNo)							                .append(TAB);	//eu_vat_no
		sb.append("")							    	            .append(TAB);	//payment_allowance_limit //requestedPaymentTerms ???
		sb.append(ccdElm.getAttribute("creditSettleTypeCd"))	    .append(TAB);	//credit_settle_type_cd
		sb.append("")							    	            .append(TAB);	//credit_settle_period_cd //requestedPaymentType ???
		sb.append(ccdElm.getAttribute("creditSettleFrequencyCd"))	.append(TAB);	//credit_settle_frequency_cd
		sb.append("")							    	            .append(TAB);	//payment_allowance_cd
		sb.append("")							    	            .append(TAB);	//credit_limit_exhaustion
		sb.append(ccdElm.getAttribute("checkoutCheckCD"))		    .append(TAB);	//checkout_check_cd
		sb.append(ccdElm.getAttribute("blockingReasonCD"))		    .append(TAB);	//blocking_reason_cd
		sb.append(ccdElm.getAttribute("branchID"))   	            .append(TAB);	//branch_id
		sb.append(ccdElm.getAttribute("branchFamilyID"))            .append(TAB);	//branch_family_id
		sb.append(df.format(regDate))					            .append(TAB);	//registration_date
		sb.append(comment)							                .append(TAB);	//comment_for_cust							
		sb.append("")							    	            .append(TAB);	//trading_partner_id
		sb.append(ccdElm.getAttribute("typeCD"))		            .append(TAB);	//type_cd
		sb.append("")							    	            .append(TAB);	//metro_club_type_id
		sb.append("")							    	            .append(TAB);	//account_holder
		sb.append("")							    	            .append(TAB);	//address_type_cd
		sb.append("")							    	            .append(TAB);	//bank_account_no
		sb.append("")							    	            .append(TAB);	//bank_identifier_cd
		sb.append("")							    	            .append(TAB);	//bank_name		
		sb.append("")							    	            .append(TAB);	//bank_routing_cd																
		sb.append("")							    	            .append(TAB);	//bank_routing_supplement
		sb.append("")							    	            .append(TAB);	//commercial_name_1
		sb.append("")							    	            .append(TAB);	//commercial_name_2
		sb.append("")							    	            .append(TAB);	//control_id
		sb.append("")							    	            .append(TAB);	//iban
		sb.append("")									            .append(TAB);	//limit_account_ind
		sb.append(ccdElm.getAttribute("mobilePhoneNo"))	            .append(TAB);	//mobile_phone_no
		sb.append(countryCd+"_Company")					            .append(TAB);	//namepart2
		sb.append(ccdElm.getAttribute("numAuthPerson"))				.append(TAB);	//numberofcards
		sb.append("")							    	            .append(TAB);	//owner_salutation_cd
		sb.append("")							    	            .append(TAB);	//parent_store_key
		sb.append("")							    	            .append(TAB);	//parent_cust_no
		sb.append(ccdElm.getAttribute("phoneNo"))		            .append(TAB);	//phone_no
		sb.append("")							    	            .append(TAB);	//limit_type_indicator
		sb.append("")							    	            .append(TAB);	//invoice_type
		sb.append("")							    	            .append(TAB);	//title_name		
		
		return sb.toString();
	}

	
	private String null2zero(String val) {
		
		return val==""?"0":val;
	}
	
}
