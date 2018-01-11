package org.citydb.gui.components.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.database.schema.mapping.AppSchema;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.Namespace;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.gui.components.checkboxtree.CheckboxTree;
import org.citydb.registry.ObjectRegistry;
import org.citygml4j.model.module.citygml.BridgeModule;
import org.citygml4j.model.module.citygml.BuildingModule;
import org.citygml4j.model.module.citygml.CityFurnitureModule;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.model.module.citygml.CityObjectGroupModule;
import org.citygml4j.model.module.citygml.GenericsModule;
import org.citygml4j.model.module.citygml.LandUseModule;
import org.citygml4j.model.module.citygml.ReliefModule;
import org.citygml4j.model.module.citygml.TransportationModule;
import org.citygml4j.model.module.citygml.TunnelModule;
import org.citygml4j.model.module.citygml.VegetationModule;
import org.citygml4j.model.module.citygml.WaterBodyModule;

@SuppressWarnings("serial")
public class FeatureTypeTree extends CheckboxTree {
	private final SchemaMapping schemaMapping;
	private final ADEExtensionManager adeManager;

	private DefaultMutableTreeNode root;
	private List<DefaultMutableTreeNode> leafs;
	private CityGMLVersion version;

	public FeatureTypeTree(boolean withADESupport) {
		schemaMapping = (SchemaMapping)ObjectRegistry.getInstance().lookup(SchemaMapping.class.getName());
		adeManager = ADEExtensionManager.getInstance();

		populate(withADESupport);
	}

	public FeatureTypeTree() {
		this(true);
	}

	public FeatureTypeTree(CityGMLVersion version, boolean withADESupport) {
		this(withADESupport);
		this.version = version;
	}

	public FeatureTypeTree(CityGMLVersion version) {
		this(version, true);
	}

	public void updateCityGMLVersion(CityGMLVersion version, boolean refresh) {
		this.version = version;
		if (refresh) {
			setPathsEnabled(true);
			repaint();
		}
	}

	public List<FeatureType> getFeatureTypes() {
		List<FeatureType> featureTypes = new ArrayList<>();
		for (DefaultMutableTreeNode leaf : leafs)
			featureTypes.add((FeatureType)leaf.getUserObject());

		return featureTypes;
	}

	public List<FeatureType> getSelectedFeatureTypes() {
		List<FeatureType> selection = new ArrayList<>();
		for (DefaultMutableTreeNode leaf : leafs) {
			if (getCheckingModel().isPathChecked(new TreePath(leaf.getPath())))
				selection.add((FeatureType)leaf.getUserObject());
		}

		return selection;
	}

	public List<QName> getSelectedTypeNames() {
		List<QName> selection = new ArrayList<>();
		for (FeatureType featureType : getSelectedFeatureTypes()) {
			if (version != null) {
				Namespace namespace = featureType.getSchema().getNamespace(version);
				if (namespace != null) {
					selection.add(new QName(namespace.getURI(), featureType.getPath()));
					continue;
				}
			}

			for (Namespace namespace : featureType.getSchema().getNamespaces())
				selection.add(new QName(namespace.getURI(), featureType.getPath()));
		}

		return selection;
	}

	public void setSelected(Set<QName> typeNames) {
		if (typeNames != null) {
			for (QName name : typeNames) {
				FeatureType featureType = schemaMapping.getFeatureType(name);
				if (featureType != null)
					setSelected(featureType);
			}
		}
	}

	public void setSelected(FeatureType featureType) {
		for (DefaultMutableTreeNode node : leafs) {
			if (node.getUserObject() == featureType) {
				getCheckingModel().addCheckingPath(new TreePath(node.getPath()));
				propagateBottomUp(node, true, true);
			}
		}
	}

	public void setPathsEnabled(boolean enable) {
		for (DefaultMutableTreeNode leaf : leafs) {
			boolean canEnable = enable && canBeEnabled((FeatureType)leaf.getUserObject());			
			getCheckingModel().setPathEnabled(new TreePath(leaf.getPath()), canEnable);
			propagateBottomUp(leaf, false, canEnable);
		}

		getCheckingModel().setPathEnabled(new TreePath(root), enable);
	}

	public void setPathEnabled(String name, String namespaceURI, boolean enable) {
		FeatureType featureType = schemaMapping.getFeatureType(name, namespaceURI);
		if (featureType != null)
			setPathEnabled(featureType, enable);
	}

	public void setPathEnabled(FeatureType featureType, boolean enable) {
		if (enable && !canBeEnabled(featureType))
			return;

		for (DefaultMutableTreeNode leaf : leafs) {
			if (leaf.getUserObject() == featureType) {
				getCheckingModel().setPathEnabled(new TreePath(leaf.getPath()), enable);
				propagateBottomUp(leaf, false, enable);
			}
		}
	}

	private boolean canBeEnabled(FeatureType featureType) {
		return version != null ? featureType.isAvailableForCityGML(version) : true;
	}

	private void propagateBottomUp(DefaultMutableTreeNode node, boolean isSelection, boolean enable) {
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
		if (parent == root)
			return;

		boolean allEnabled = enable;
		for (int i = 0; i < parent.getChildCount(); ++i) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)parent.getChildAt(i);
			TreePath path = new TreePath(child.getPath());
			boolean check = isSelection ? getCheckingModel().isPathChecked(path) : getCheckingModel().isPathEnabled(path);			
			if (check != enable) {
				allEnabled = !enable;
				break;
			}
		}

		if (allEnabled == enable) {
			TreePath path = new TreePath(parent.getPath());
			if (isSelection)
				getCheckingModel().addCheckingPath(path);
			else
				getCheckingModel().setPathEnabled(path, enable);

			propagateBottomUp(parent, isSelection, enable);
		}
	}	

	private void populate(boolean withADESupport) {
		root = new DefaultMutableTreeNode("CityObject");
		leafs = new ArrayList<>();

		root.add(getCityGMLModuleNode(BridgeModule.v2_0_0));
		root.add(getCityGMLModuleNode(BuildingModule.v2_0_0));
		root.add(getCityGMLModuleNode(CityFurnitureModule.v2_0_0));
		root.add(getCityGMLModuleNode(CityObjectGroupModule.v2_0_0));
		root.add(getCityGMLModuleNode(GenericsModule.v2_0_0));
		root.add(getCityGMLModuleNode(LandUseModule.v2_0_0));
		root.add(getCityGMLModuleNode(ReliefModule.v2_0_0));
		root.add(getCityGMLModuleNode(TransportationModule.v2_0_0));
		root.add(getCityGMLModuleNode(TunnelModule.v2_0_0));
		root.add(getCityGMLModuleNode(VegetationModule.v2_0_0));
		root.add(getCityGMLModuleNode(WaterBodyModule.v2_0_0));

		if (withADESupport) {
			for (DefaultMutableTreeNode adeNode : getADENodes())
				root.add(adeNode);
		}

		((DefaultTreeModel)getModel()).setRoot(root);
	}

	private DefaultMutableTreeNode getCityGMLModuleNode(CityGMLModule module) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(module.getType());

		AppSchema schema = schemaMapping.getSchema(module.getNamespaceURI());
		if (schema != null) {
			for (FeatureType featureType : schema.listTopLevelFeatureTypes(true)) {
				DefaultMutableTreeNode child = new DefaultMutableTreeNode(featureType);
				node.add(child);
				leafs.add(child);
			}
		}

		return node;
	}

	private List<DefaultMutableTreeNode> getADENodes() {
		List<DefaultMutableTreeNode> adeNodes = new ArrayList<>();

		for (ADEExtension extension : adeManager.getExtensions()) {
			DefaultMutableTreeNode adeNode = new DefaultMutableTreeNode(extension.getMetadata().getName());

			for (AppSchema schema : extension.getSchemas()) {
				for (FeatureType featureType : schema.listTopLevelFeatureTypes(true)) {
					DefaultMutableTreeNode child = new DefaultMutableTreeNode(featureType); 
					adeNode.add(child);
					leafs.add(child);
				}
			}

			if (adeNode.getChildCount() > 0)
				adeNodes.add(adeNode);
		}

		return adeNodes;
	}

}
