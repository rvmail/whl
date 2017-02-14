package com.dopool.icntvoverseas.recomview.test;

import java.util.ArrayList;

import dopool.cntv.recommendation.model.RecomBoxLayout;
import dopool.cntv.recommendation.model.RecomPanel;

/**
 * 模拟测试数据
 * 
 * @author VIC
 * 
 */
public class RecomPanelGenerator {

	public static RecomPanel generateMockData(int heightWeight) {
		RecomPanel panel = new RecomPanel("1", 10, heightWeight, "MockData");

		ArrayList<RecomBoxLayout> itemBoxes = new ArrayList<RecomBoxLayout>();

		RecomBoxLayout item1 = new RecomBoxLayout("1_1", 1, 1, 3, 2);
		item1.setDefaultFocus(true);
		item1.setMockData(true);
		itemBoxes.add(item1);

		RecomBoxLayout item2 = new RecomBoxLayout("1_2", 4, 1, 2, 1);
		item2.setMockData(true);
		itemBoxes.add(item2);

		RecomBoxLayout item3 = new RecomBoxLayout("1_3", 4, 2, 1, 1);
		item3.setMockData(true);
		itemBoxes.add(item3);

		RecomBoxLayout item4 = new RecomBoxLayout("1_4", 5, 2, 1, 1);
		item4.setMockData(true);
		itemBoxes.add(item4);

		RecomBoxLayout item5 = new RecomBoxLayout("1_5", 6, 1, 1, 1);
		item5.setMockData(true);
		itemBoxes.add(item5);

		RecomBoxLayout item6 = new RecomBoxLayout("1_6", 6, 2, 1, 1);
		item6.setMockData(true);
		itemBoxes.add(item6);

		RecomBoxLayout item7 = new RecomBoxLayout("1_7", 7, 1, 1, 2);
		item7.setMockData(true);
		itemBoxes.add(item7);

		RecomBoxLayout item8 = new RecomBoxLayout("1_8", 8, 1, 2, 2);
		item8.setMockData(true);
		itemBoxes.add(item8);

		panel.setLayoutBoxes(itemBoxes);
		return panel;
	}

	public static RecomPanel generateRealData(int heightWeight) {

		RecomPanel panel = new RecomPanel("2", 10, heightWeight, "RealData");

		ArrayList<RecomBoxLayout> itemBoxes = new ArrayList<RecomBoxLayout>();

		RecomBoxLayout item1 = new RecomBoxLayout("2_1", 1, 1, 3, 2);
		item1.setDefaultFocus(true);
		itemBoxes.add(item1);

		RecomBoxLayout item2 = new RecomBoxLayout("2_2", 4, 1, 2, 1);
		itemBoxes.add(item2);

		RecomBoxLayout item3 = new RecomBoxLayout("2_3", 4, 2, 1, 1);
		itemBoxes.add(item3);

		RecomBoxLayout item4 = new RecomBoxLayout("2_4", 5, 2, 1, 1);
		itemBoxes.add(item4);

		RecomBoxLayout item5 = new RecomBoxLayout("2_5", 6, 1, 1, 1);
		itemBoxes.add(item5);

		RecomBoxLayout item6 = new RecomBoxLayout("2_6", 6, 2, 1, 1);
		itemBoxes.add(item6);

		RecomBoxLayout item7 = new RecomBoxLayout("2_7", 7, 1, 1, 2);
		itemBoxes.add(item7);

		RecomBoxLayout item8 = new RecomBoxLayout("2_8", 8, 1, 2, 2);
		itemBoxes.add(item8);

		panel.setLayoutBoxes(itemBoxes);

		return panel;
	}

}
