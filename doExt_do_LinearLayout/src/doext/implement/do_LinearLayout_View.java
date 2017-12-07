package doext.implement;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;
import core.DoServiceContainer;
import core.helper.DoJsonHelper;
import core.helper.DoScriptEngineHelper;
import core.helper.DoTextHelper;
import core.helper.DoUIModuleHelper;
import core.helper.DoUIModuleHelper.LayoutParamsType;
import core.interfaces.DoIPageView;
import core.interfaces.DoIScriptEngine;
import core.interfaces.DoIUIModuleView;
import core.object.DoInvokeResult;
import core.object.DoUIModule;
import doext.define.do_LinearLayout_IMethod;
import doext.define.do_LinearLayout_MAbstract;

/**
 * 自定义扩展UIView组件实现类，此类必须继承相应VIEW类，并实现DoIUIModuleView,do_LinearLayout_IMethod接口；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.model.getUniqueKey());
 */
public class do_LinearLayout_View extends LinearLayout implements DoIUIModuleView, do_LinearLayout_IMethod, OnClickListener, OnLongClickListener {

	/**
	 * 每个UIview都会引用一个具体的model实例；
	 */
	private do_LinearLayout_MAbstract model;
	private DoIPageView pageView;
	private double xZoom;
	private double yZoom;
	private String target;

	public do_LinearLayout_View(Context context) {
		super(context);
		this.setOrientation(VERTICAL);
	}

	public DoIPageView getPageView() {
		return pageView;
	}

	public void setPageView(DoIPageView pageView) {
		this.pageView = pageView;
	}

	/**
	 * 初始化加载view准备,_doUIModule是对应当前UIView的model实例
	 */
	@Override
	public void loadView(DoUIModule _doUIModule) throws Exception {
		this.model = (do_LinearLayout_MAbstract) _doUIModule;
		this.setOnClickListener(this);
		this.setOnLongClickListener(this);
		xZoom = this.model.getInnerXZoom();
		yZoom = this.model.getInnerYZoom();
		reDraw();
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (!isEnabled() || (!this.model.getEventCenter().containsEvent("touch"))) {
			return false;
		}
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			doLinearLayout_TouchDown();
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			doLinearLayout_TouchUp();
			break;
		}
		return super.onTouchEvent(ev);
	}

	private void reDraw() {
		// 从外向内画子控件
		for (int i = 0; i < this.model.getChildUIModules().size(); i++) {
			DoUIModule _childUI = this.model.getChildUIModules().get(i);
			View _view = (View) _childUI.getCurrentUIModuleView();
			_childUI.setLayoutParamsType(LayoutParamsType.LinearLayout.toString());
			DoUIModuleHelper.removeFromSuperview(_view);
			this.addView(_view);
		}
	}

	/**
	 * 动态修改属性值时会被调用，方法返回值为true表示赋值有效，并执行onPropertiesChanged，否则不进行赋值；
	 * 
	 * @_changedValues<key,value>属性集（key名称、value值）；
	 */
	@Override
	public boolean onPropertiesChanging(Map<String, String> _changedValues) {
		return true;
	}

	/**
	 * 属性赋值成功后被调用，可以根据组件定义相关属性值修改UIView可视化操作；
	 * 
	 * @_changedValues<key,value>属性集（key名称、value值）；
	 */
	@Override
	public void onPropertiesChanged(Map<String, String> _changedValues) {
		DoUIModuleHelper.handleBasicViewProperChanged(this.model, _changedValues);
		if (_changedValues.containsKey("direction")) {
			String _direction = _changedValues.get("direction");
			if ("horizontal".equals(_direction)) {
				this.setOrientation(LinearLayout.HORIZONTAL);
			} else {
				this.setOrientation(LinearLayout.VERTICAL);
			}
		}

		if (_changedValues.containsKey("enabled")) {
			boolean _isEnable = DoTextHelper.strToBool(_changedValues.get("enabled"), false);
			this.setEnabled(_isEnable);
		}

		if (_changedValues.containsKey("padding")) {
			int[] _padding = DoTextHelper.split(_changedValues.get("padding"));
			int _top = (int) DoUIModuleHelper.getCalcValue(_padding[0] * yZoom);
			int _left = (int) DoUIModuleHelper.getCalcValue(_padding[1] * xZoom);
			int _bottom = (int) DoUIModuleHelper.getCalcValue(_padding[2] * yZoom);
			int _right = (int) DoUIModuleHelper.getCalcValue(_padding[3] * xZoom);

			this.setPadding(_left, _top, _right, _bottom);
		}

		if (_changedValues.containsKey("bgImage") || _changedValues.containsKey("bgImageFillType")) {
			try {
				DoUIModuleHelper.setBgImage(this.model, _changedValues);
			} catch (Exception _err) {
				DoServiceContainer.getLogEngine().writeError("DoLinearLayout setBgImage \n", _err);
			}
		}
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("add".equals(_methodName)) {
			this.add(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return false;
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.model.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) {
		// ...do something
		return false;
	}

	/**
	 * 释放资源处理，前端JS脚本调用closePage或执行removeui时会被调用；
	 */
	@Override
	public void onDispose() {
		// ...do something
	}

	/**
	 * 重绘组件，构造组件时由系统框架自动调用；
	 * 或者由前端JS脚本调用组件onRedraw方法时被调用（注：通常是需要动态改变组件（X、Y、Width、Height）属性时手动调用）
	 * 
	 * @throws Exception
	 */
	@Override
	public void onRedraw() throws Exception {
		if (this.model.getLayoutParamsType() != null) {
			this.setLayoutParams(DoUIModuleHelper.getLayoutParams(this.model));
		}
		for (int i = 0; i < this.model.getChildUIModules().size(); i++) {
			DoUIModule _childUI = this.model.getChildUIModules().get(i);
			_childUI.getCurrentUIModuleView().onRedraw();
		}
	}

	/**
	 * 获取当前model实例
	 */
	@Override
	public DoUIModule getModel() {
		return model;
	}

	// =========================================================================
	/**
	 * 插入一个UI；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void add(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {

		String _id = DoJsonHelper.getString(_dictParas, "id", "");
		String _viewtemplate = DoJsonHelper.getString(_dictParas, "path", "");
		target = DoJsonHelper.getString(_dictParas, "target", "");
		String _address = model.addUI(_scriptEngine, _viewtemplate, null, null, _id);
		reDraw();
		onRedraw();
		_invokeResult.setResultText(_address);
	}

	public void addSubview(DoUIModule _insertViewModel) throws Exception {

		List<DoUIModule> _childUIModules = this.model.getChildUIModules();
		if (target != null && !target.equals("")) {// 插入的jui加在该组件的下面
			DoUIModule _targetUIModule = DoScriptEngineHelper.parseUIModule(model.getCurrentPage().getScriptEngine(), model.getCurrentUIContainer().getRootView().getUniqueKey() + "." + target);
			if (_targetUIModule == null) {
				// 没找到target对应的组件
				_childUIModules.add(0, _insertViewModel);
				return;
			}

			for (int i = 0; i < _childUIModules.size(); i++) {
				String _uniqueKey = _childUIModules.get(i).getUniqueKey();
				if (_uniqueKey != null && _targetUIModule.getUniqueKey().equals(_uniqueKey)) {
					_childUIModules.add(i + 1, _insertViewModel);
					break;
				}
			}

		} else {// 为空时表示加在Linearlayout最上面
			_childUIModules.add(0, _insertViewModel);
		}
	}

	@Override
	public void onClick(View v) {
		doLinearLayout_Touch();
	}

	// =========================================================================
	private void doLinearLayout_Touch() {
		DoInvokeResult _invokeResult = new DoInvokeResult(this.model.getUniqueKey());
		this.model.getEventCenter().fireEvent("touch", _invokeResult);
	}

	private void doLinearLayout_LongTouch() {
		DoInvokeResult _invokeResult = new DoInvokeResult(this.model.getUniqueKey());
		this.model.getEventCenter().fireEvent("longTouch", _invokeResult);
	}

	private void doLinearLayout_TouchUp() {
		DoInvokeResult _invokeResult = new DoInvokeResult(this.model.getUniqueKey());
		this.model.getEventCenter().fireEvent("touchUp", _invokeResult);
	}

	private void doLinearLayout_TouchDown() {
		DoInvokeResult _invokeResult = new DoInvokeResult(this.model.getUniqueKey());
		this.model.getEventCenter().fireEvent("touchDown", _invokeResult);
	}

	@Override
	public boolean onLongClick(View arg0) {
		doLinearLayout_LongTouch();
		return true;
	}
}