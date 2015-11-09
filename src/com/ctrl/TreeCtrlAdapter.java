package com.ctrl;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.manniu.manniu.R;

public class TreeCtrlAdapter extends BaseAdapter {
	@SuppressWarnings("unused")
	private Context context = null;
	private List<TreeNode> nodeList = new ArrayList<TreeNode>(); // 所有的节点
	private List<TreeNode> nodeListToShow = new ArrayList<TreeNode>(); // 要展现的节点
	private LayoutInflater inflater = null;
	private TreeNode root = null;

	public TreeCtrlAdapter(Context con, TreeNode Root, int layout) {
		this.context = con;
		this.inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		establishNodeList(Root);
		this.root = Root;
		setNodeListToShow();
	}

	public void establishNodeList(TreeNode node) {
		nodeList.add(node);
		if (node.isLeafOrNot())
			return;
		List<TreeNode> children = node.getChildren();
		for (int i = 0; i < children.size(); i++) {
			establishNodeList(children.get(i));
		}
	}

	public void setNodeListToShow() {
		this.nodeListToShow.clear();
		establishNodeListToShow(this.root);
		this.notifyDataSetInvalidated();
	}

	// 构造要展示在listview的nodeListToShow
	public void establishNodeListToShow(TreeNode node) {
		this.nodeListToShow.add(node);
		if (node.getExpanded() && !node.isLeafOrNot() && node.getChildren() != null) {
			List<TreeNode> children = node.getChildren();
			for (int i = 0; i < children.size(); i++) {
				establishNodeListToShow(children.get(i));
			}
		}
	}

	// 根据oid得到某一个Node,并更改其状态
	public void changeNodeExpandOrFold(int position) {
		TreeNode node = this.nodeListToShow.get(position);
		if (node != null) {
			boolean flag = node.getExpanded();
			node.setExpanded(!flag);
		}
	}

	public void Expand(int position, boolean bExpand) {
		TreeNode node = this.nodeListToShow.get(position);
		if (node != null) {
			node.setExpanded(bExpand);
		}
	}

	// listItem被点击的响应事件
	public TreeNode OnListItemClick(int position) {
		TreeNode node = this.nodeListToShow.get(position);
		if (node.isLeafOrNot()) {
			// 处理snmp代码
			//Toast.makeText(this.context, "该节点为子节点", Toast.LENGTH_SHORT).show();
			return node;
		} else {
			this.changeNodeExpandOrFold(position);
			this.setNodeListToShow();
			this.notifyDataSetChanged();
			return null;
		}
	}

	@Override
	public int getCount() {
		return nodeListToShow.size();
	}

	@Override
	public TreeNode getItem(int position) {
		return nodeListToShow.get(position);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		Holder holder = null;
		if (view != null) {
			holder = (Holder) view.getTag();
		} else {
			holder = new Holder();
			view = this.inflater.inflate(R.layout.treeview_item, null);
			holder.title = (TextView) view.findViewById(R.id.textview_nodeDescription);
			holder.nodeIcon = (ImageView) view.findViewById(R.id.imageview_nodeImage);
			holder.expandOrFoldIcon = (ImageView) view.findViewById(R.id.imageview_expandedImage);
			view.setTag(holder);
		}

		// 绘制一个item

		// 设置文字
		TreeNode node = this.nodeListToShow.get(position);
		holder.title.setText(node.getTitle());

		// 设置图标
		int icon = node.getIcon();
		if (icon != -1) {
			holder.nodeIcon.setImageResource(icon);
			holder.nodeIcon.setVisibility(View.VISIBLE);
		} else
			holder.nodeIcon.setVisibility(View.INVISIBLE);

		// 设置展开折叠图标
		if (!node.isLeafOrNot()) {
			int expandIcon = node.getExpandOrFoldIcon();
			if (expandIcon == -1)
				holder.expandOrFoldIcon.setVisibility(View.INVISIBLE);
			else {
				holder.expandOrFoldIcon.setImageResource(expandIcon);
				holder.expandOrFoldIcon.setVisibility(View.VISIBLE);
			}

		} else {
			holder.expandOrFoldIcon.setVisibility(View.INVISIBLE);
		}
		view.setPadding(node.getLevel() * 35, 10, 10, 10);
		return view;
	}

	public class Holder {
		TextView title;
		ImageView nodeIcon;
		ImageView expandOrFoldIcon;
	}

}