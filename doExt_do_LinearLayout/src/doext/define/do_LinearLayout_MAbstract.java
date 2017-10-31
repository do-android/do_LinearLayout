package doext.define;

import core.object.DoProperty;
import core.object.DoProperty.PropertyDataType;
import core.object.DoUIModuleCollection;


public abstract class do_LinearLayout_MAbstract extends DoUIModuleCollection{

	protected do_LinearLayout_MAbstract() throws Exception {
		super();
	}
	
	/**
	 * 初始化
	 */
	@Override
	public void onInit() throws Exception{
        super.onInit();
        //注册属性
		this.registProperty(new DoProperty("bgImage", PropertyDataType.String, "", false));
		this.registProperty(new DoProperty("bgImageFillType", PropertyDataType.String, "fillxy", false));
		this.registProperty(new DoProperty("direction", PropertyDataType.String, "vertical", true));
		this.registProperty(new DoProperty("enabled", PropertyDataType.Bool, "true", false));
		this.registProperty(new DoProperty("padding", PropertyDataType.String, "0,0,0,0", true));
	}
}