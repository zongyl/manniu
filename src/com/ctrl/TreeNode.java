package com.ctrl;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
	private TreeNode parent = null;// 父节点
	private List<TreeNode> children = null;
	private String name = null; // 该节点信息的描述
	private int regionId;
	private Object value = null; // 该节点的值
	private boolean isLeaf = false; // 是否为叶节点
	private boolean isExpanded = true; // 该节点是否展开
	private int icon = -1; // 该节点的图标对应的id
	//private int iconForExpandedOrFolded = -1;
	private int iconForExpanding = -1;
	private int iconForFolding = -1;
	private boolean tableItemOrNot = false;// 表示是否为表结构的一列

	public TreeNode(TreeNode parent, String title, int icon, Object userData, int exIcon, int foIcon) {
		InitParam(parent, title, 0,userData, icon, exIcon, foIcon);
	}

	public TreeNode(TreeNode parent, String title, int icon) {
		InitParam(parent, title,0, null, icon, -1, -1);
	}

//	public TreeNode(TreeNode parent, String title, int icon, Object userData) {
//		InitParam(parent, title, userData, icon, -1, -1);
//	}
	//添加是否展开
	public TreeNode(TreeNode parent, String title,int regionId, int icon, Object userData,boolean isExpanded) {
		setExpanded(isExpanded);
		InitParam(parent, title,regionId, userData, icon, -1, -1);
	}
	
//	public TreeNode(TreeNode parent, String title, int icon, Object userData,boolean isExpanded) {
//		setExpanded(isExpanded);
//		InitParam(parent, title, userData, icon, -1, -1);
//	}

	public void InitParam(TreeNode parent, String title,int regionId, Object userData, int icon, int exIcon, int foIcon) {
		if (parent != null) {
			parent.addChildNode(this);
		}
		this.parent = parent;
		this.name = title;
		this.regionId = regionId;
		this.value = userData;
		this.isLeaf = true;
		this.icon = icon;
		if (exIcon == -1) {
			if (parent != null) {
				this.iconForExpanding = parent.iconForExpanding;
			}
		} else {
			this.iconForExpanding = exIcon;
		}

		if (foIcon == -1) {
			if (parent != null) {
				this.iconForFolding = parent.iconForFolding;
			}
		} else {
			this.iconForFolding = foIcon;
		}
	}

	public void setTableItemOrNot(boolean tableItemOrNot) {
		this.tableItemOrNot = tableItemOrNot;
	}

	public boolean getTableItemOrNot() {
		return this.tableItemOrNot;
	}

	// 设置value
	public void setValue(Object value) {
		this.value = value;
	}

	// 得到value
	public Object getValue() {
		return this.value;
	}

	// 设置图标
	public void setIcon(int icon) {
		this.icon = icon;
	}

	public int getIcon() {
		return this.icon;
	}

	// 得到description
	public String getTitle() {
		return this.name;
	}
	
	public int getRegionId(){
		return this.regionId;
	}

	// 得到是否为叶节点
	public boolean isLeafOrNot() {
		return this.isLeaf;
	}

	// 得到当前节点所在的层数，根为0层
	public int getLevel() {
		return parent == null ? 0 : parent.getLevel() + 1;
	}

	// 设置是否展开
	public void setExpanded(boolean isExpanded) {
		this.isExpanded = isExpanded;
	}

	public boolean getExpanded() {
		return this.isExpanded;
	}

	// 添加子节点
	private void addChildNode(TreeNode child) {
		if (this.children == null) {
			isLeaf = false;
			this.children = new ArrayList<TreeNode>();
		}
		this.children.add(child);
	}

	// 清空子节点
	public void clearChildren() {
		if (children != null) {
			this.children.clear();
		}
	}

	// 是否为根节点
	public boolean isRoot() {
		return this.parent.equals(null) ? true : false;
	}

	// 设置展开图标
	public void setExpandIcon(int expand) {
		this.iconForExpanding = expand;
	}

	// 设置折叠图标
	public void setFoldIcon(int fold) {
		this.iconForFolding = fold;
	}

	// 得到展开或折叠图标
	public int getExpandOrFoldIcon() {
		if (this.isExpanded == true)
			return this.iconForExpanding;
		else
			return this.iconForFolding;
	}

	// 得到子树
	public List<TreeNode> getChildren() {
		return this.children;
	}
}