package doext.implement;

import core.object.DoUIModule;
import doext.define.do_LinearLayout_MAbstract;

/**
 * 自定义扩展组件Model实现，继承do_LinearLayout_MAbstract抽象类；
 *
 */
public class do_LinearLayout_Model extends do_LinearLayout_MAbstract {

	public do_LinearLayout_Model() throws Exception {
		super();
	}
	
	
	@Override
	public void addSubview(DoUIModule _insertViewModel) throws Exception {
		do_LinearLayout_View _view = (do_LinearLayout_View) this.getCurrentUIModuleView();
		_view.addSubview(_insertViewModel);
	}
}
