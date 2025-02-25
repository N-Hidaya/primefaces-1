/*
 * The MIT License
 *
 * Copyright (c) 2009-2021 PrimeTek
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primefaces.component.inplace;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.*;

public class InplaceRenderer extends CoreRenderer {

    @Override
    public void decode(FacesContext context, UIComponent component) {
        decodeBehaviors(context, component);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        Inplace inplace = (Inplace) component;

        encodeMarkup(context, inplace);
        encodeScript(context, inplace);
    }

    protected void encodeMarkup(FacesContext context, Inplace inplace) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = inplace.getClientId(context);
        String widgetVar = inplace.resolveWidgetVar(context);

        String userStyle = inplace.getStyle();
        String styleClass = getStyleClassBuilder(context).add(Inplace.CONTAINER_CLASS, inplace.getStyleClass()).build();
        boolean disabled = inplace.isDisabled();
        String displayClass = disabled ? Inplace.DISABLED_DISPLAY_CLASS : Inplace.DISPLAY_CLASS;

        boolean validationFailed = context.isValidationFailed() && !inplace.isValid();
        String mode = inplace.getMode();

        //container
        writer.startElement("span", inplace);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute("class", styleClass, "id");
        if (userStyle != null) {
            writer.writeAttribute("style", userStyle, "id");
        }

        writer.writeAttribute(HTML.WIDGET_VAR, widgetVar, null);


        //output
        String outputStyle = validationFailed
                ? Inplace.DISPLAY_NONE
                : (Inplace.MODE_OUTPUT.equals(mode) ? Inplace.DISPLAY_INLINE : Inplace.DISPLAY_NONE);
        UIComponent outputFacet = inplace.getFacet("output");

        writer.startElement("span", null);
        writer.writeAttribute("id", clientId + "_display", "id");
        writer.writeAttribute("class", displayClass, null);
        writer.writeAttribute("style", "display:" + outputStyle, null);
        if (inplace.getTabindex() != null) {
            writer.writeAttribute("tabindex", inplace.getTabindex(), null);
            writer.writeAttribute("role", "button", null);
        }

        if (ComponentUtils.shouldRenderFacet(outputFacet)) {
            outputFacet.encodeAll(context);
        }
        else {
            writer.writeText(getLabelToRender(context, inplace), null);
        }

        writer.endElement("span");


        //input
        String inputStyle = validationFailed
                ? Inplace.DISPLAY_INLINE
                : (Inplace.MODE_OUTPUT.equals(mode) ? Inplace.DISPLAY_NONE : Inplace.DISPLAY_INLINE);
        UIComponent inputFacet = inplace.getFacet("input");

        if (!inplace.isDisabled()) {
            writer.startElement("span", null);
            writer.writeAttribute("id", clientId + "_content", "id");
            writer.writeAttribute("class", Inplace.CONTENT_CLASS, null);
            writer.writeAttribute("style", "display:" + inputStyle, null);

            if (ComponentUtils.shouldRenderFacet(inputFacet)) {
                inputFacet.encodeAll(context);
            }
            else {
                renderChildren(context, inplace);
            }

            if (inplace.isEditor()) {
                encodeEditor(context, inplace);
            }

            writer.endElement("span");
        }

        writer.endElement("span");
    }

    protected String getLabelToRender(FacesContext context, Inplace inplace) {
        String label = inplace.getLabel();
        if (!isValueBlank(label)) {
            return label;
        }

        String value = ComponentUtils.getValueToRender(context, inplace.getChildren().get(0));
        if (LangUtils.isBlank(value)) {
            String emptyLabel = inplace.getEmptyLabel();
            if (emptyLabel != null) {
                return emptyLabel;
            }

            return Constants.EMPTY_STRING;
        }

        return value;
    }

    protected void encodeScript(FacesContext context, Inplace inplace) throws IOException {
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("Inplace", inplace)
                .attr("effect", inplace.getEffect())
                .attr("effectSpeed", inplace.getEffectSpeed())
                .attr("event", inplace.getEvent())
                .attr("toggleable", inplace.isToggleable(), false)
                .attr("disabled", inplace.isDisabled(), false)
                .attr("editor", inplace.isEditor(), false);

        encodeClientBehaviors(context, inplace);

        wb.finish();
    }

    protected void encodeEditor(FacesContext context, Inplace inplace) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement("span", null);
        writer.writeAttribute("id", inplace.getClientId(context) + "_editor", null);
        writer.writeAttribute("class", Inplace.EDITOR_CLASS, null);

        encodeButton(context, inplace.getSaveLabel(), Inplace.SAVE_BUTTON_CLASS, "ui-icon-check");
        encodeButton(context, inplace.getCancelLabel(), Inplace.CANCEL_BUTTON_CLASS, "ui-icon-close");

        writer.endElement("span");
    }

    protected void encodeButton(FacesContext context, String title, String styleClass, String icon) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement("button", null);
        writer.writeAttribute("type", "button", null);
        writer.writeAttribute("class", HTML.BUTTON_ICON_ONLY_BUTTON_CLASS + " " + styleClass, null);
        writer.writeAttribute("title", title, null);

        //icon
        writer.startElement("span", null);
        writer.writeAttribute("class", HTML.BUTTON_LEFT_ICON_CLASS + " " + icon, null);
        writer.endElement("span");

        //text
        writer.startElement("span", null);
        writer.writeAttribute("class", HTML.BUTTON_TEXT_CLASS, null);
        writer.write("ui-button");
        writer.endElement("span");

        writer.endElement("button");
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        //Do Nothing
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
