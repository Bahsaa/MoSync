/* Copyright 2013 David Axmark

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.mosync.nativeui.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.mosync.internal.generated.IX_WIDGET;
import com.mosync.nativeui.util.LayoutParamsSetter;
import com.mosync.nativeui.util.properties.InvalidPropertyValueException;
import com.mosync.nativeui.util.properties.PropertyConversionException;

/**
 * This subclass of Widget represents the behavior of a Widget
 * that can layout other widgets.
 *
 * @author fmattias
 */
public class Layout extends Widget
{
	/**
	 * A list of the children of this widget. This information
	 * is perhaps needed if we destroy the layout before
	 * destroying all of the children.
	 */
	List<Widget> m_children = new ArrayList<Widget>( );
	ScrollView m_scrollview = null;
	boolean m_scrollable = false;


	/**
	 * Constructor.
	 *
	 * @param handle handle Integer handle corresponding to this instance.
	 * @param view A layout wrapped by this widget.
	 */
	public Layout(int handle, ViewGroup view)
	{
		super( handle, view );
	}

	public void setIsScrollable(boolean s)
	{
		if(m_scrollable == s) return;

		m_scrollable = s;

		if(s == true)
		{
			m_scrollview = new ScrollView( getView( ).getContext() );
			Widget w = getParent();
			if(w != null)
			{
				ViewGroup g = (ViewGroup) w.getView( );

				for(int i = 0; i < g.getChildCount( ); i++)
				{
					View v = g.getChildAt( i );
					if( getView( ) == v )
					{
						g.removeViewAt(i);
						g.addView( m_scrollview, i );
						break;
					}
				}
			}

			m_scrollview.addView( getView( ) );
		}
		else
		{
			m_scrollview.removeView( getView( ) );

			Widget w = getParent();
			if(w != null)
			{
				ViewGroup g = (ViewGroup) w.getView( );

				for(int i = 0; i < g.getChildCount( ); i++)
				{
					View v = g.getChildAt( i );
					if( m_scrollview == v )
					{
						g.addView( getView( ) , i );
						break;
					}
				}
			}
		}
	}

	/**
	 * Check if the current layout is scrollable.
	 * @return True if scrollable.
	 */
	public boolean isScrollable()
	{
		return m_scrollable;
	}

	/**
	 * Adds a child to this layout at the given position. By
	 * default the view will be added to the wrapped ViewGroup.
	 *
	 * @param child The child to be added.
	 * @param index The index where to add the child. The child will have
	 *        this index after it has been added, the index starts at 0. If
	 *        the index is -1 it will be added at the end.
	 */
	@Override
	public int addChildAt(Widget child, int index)
	{
		int listIndex = index;
		if( index == -1 )
		{
			listIndex = m_children.size( );
		}

		child.setParent( this );
		m_children.add( listIndex, child );

		updateLayoutParamsForChild( child );

		// Add child to layout
		ViewGroup layout = getView( );
		layout.addView( child.getRootView( ), listIndex );

		return IX_WIDGET.MAW_RES_OK;
	}

	/**
	 * Check if a widget is already in the children list.
	 * @param child The widget to be checked.
	 * @return true if the widget is already in the list of children.
	 */
	public Boolean containsChild(Widget child)
	{
		return m_children.contains(child);
	}

	/**
	 * Returns the children of this layout.
	 *
	 * Note: Modifications to the returned list of children will
	 * not affect the layout.
	 *
	 * @return a list of children for this layout.
	 */
	public List<Widget> getChildren()
	{
		return new ArrayList<Widget>( m_children );
	}

	/**
	 * Returns the children count for this layout.
	 *
	 * @return The layout children count.
	 */
	public int getChildCount()
	{
		return m_children.size();
	}

	/**
	 * Updates the Android layout params for the given child, according
	 * to the parameters specified in the child. Only the parameters that
	 * are supported for the layout will be taken into consideration.
	 *
	 * @param child The child for which to update the layout params.
	 */
	public void updateLayoutParamsForChild(Widget child)
	{
		// Set layout params for the child
		ViewGroup.LayoutParams nativeLayoutParams = createNativeLayoutParams( child.getLayoutParams( ) );
		LayoutParamsSetter.setPossibleParams( child.getLayoutParams( ), nativeLayoutParams );

		View childView = child.getView();

		View rootView = child.getRootView();
		if( childView != rootView )
		{
			if ( child instanceof Layout )
			{
				if ( !((Layout) child).isScrollable() )
				{
					rootView.setLayoutParams(
							new ViewGroup.LayoutParams( nativeLayoutParams.width, nativeLayoutParams.height ) );
				}
			}
		}
		else
		{
			childView.setLayoutParams( nativeLayoutParams );
		}
	}

	/**
	 * Removes a child form this layout, by default
	 * the view of the child is removed from the wrapped
	 * ViewGroup.
	 *
	 * The child's parent will be set to null.
	 *
	 * @param child
	 */
	@Override
	public int removeChild(Widget child)
	{
		child.setParent( null );
		m_children.remove( child );
		ViewGroup layout = getView( );
		layout.removeView( child.getRootView( ) );

		return IX_WIDGET.MAW_RES_OK;
	}

	/**
	 * Updates the layout params for the given child
	 * to those suitable for this layout.
	 *
	 * @param mosyncLayoutParams The mosync layout params to create a specific layout params for.
	 */
	public ViewGroup.LayoutParams createNativeLayoutParams(LayoutParams mosyncLayoutParams)
	{
		return new ViewGroup.LayoutParams( mosyncLayoutParams.getWidth( ), mosyncLayoutParams.getHeight( ) );
	}

	/**
	 * @see Widget.isLayout.
	 */
	public boolean isLayout()
	{
		return true;
	}

	/**
	 * @see Widget.getView.
	 */
	@Override
	public ViewGroup getView()
	{
		// ViewGroup is a covariant return type to View
		return (ViewGroup) super.getView( );
	}

	/**
	 * @see Widget.getView.
	 */
	@Override
	public ViewGroup getRootView()
	{
		if(m_scrollable == true)
		{
			// ViewGroup is a covariant return type to View
			return (ViewGroup) m_scrollview;
		}
		else
		{
			return (ViewGroup) super.getView( );
		}
	}

	@Override
	public boolean setProperty(String property, String value)
			throws PropertyConversionException, InvalidPropertyValueException
	{
		if( super.setProperty( property, value ) )
		{
			return true;
		}

		if( property.equals( IX_WIDGET.MAW_VERTICAL_LAYOUT_SCROLLABLE ) )
		{
			this.setIsScrollable( value.equals( "true" ) );
		}
		else
		{
			return false;
		}

		return true;
	}
}
