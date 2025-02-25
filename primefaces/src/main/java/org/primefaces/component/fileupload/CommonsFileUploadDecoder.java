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
package org.primefaces.component.fileupload;

import java.io.File;
import org.apache.commons.fileupload.FileItem;
import org.primefaces.model.file.CommonsUploadedFile;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.webapp.MultipartRequest;

import javax.faces.context.FacesContext;
import javax.servlet.ServletRequestWrapper;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.primefaces.util.LangUtils;

public class CommonsFileUploadDecoder extends AbstractFileUploadDecoder<MultipartRequest> {

    private static final Logger LOGGER = Logger.getLogger(CommonsFileUploadDecoder.class.getName());

    @Override
    public String getName() {
        return "commons";
    }

    @Override
    protected List<UploadedFile> createUploadedFiles(MultipartRequest request, FileUpload fileUpload, String inputToDecodeId) {
        Long sizeLimit = fileUpload.getSizeLimit();
        return request.getFileItems(inputToDecodeId).stream()
                .filter(p -> LangUtils.isNotBlank(p.getName()))
                .map(p -> new CommonsUploadedFile(p, sizeLimit))
                .collect(Collectors.toList());
    }

    @Override
    protected UploadedFile createUploadedFile(MultipartRequest request, FileUpload fileUpload, String inputToDecodeId) {
        FileItem file = request.getFileItem(inputToDecodeId);
        if (file == null || LangUtils.isBlank(file.getName())) {
            return null;
        }

        return new CommonsUploadedFile(file, fileUpload.getSizeLimit());
    }

    @Override
    protected MultipartRequest getRequest(FacesContext ctxt) {
        MultipartRequest multipartRequest = null;
        Object request = ctxt.getExternalContext().getRequest();
        while (request instanceof ServletRequestWrapper) {
            if (request instanceof MultipartRequest) {
                multipartRequest = (MultipartRequest) request;
                break;
            }
            else {
                request = ((ServletRequestWrapper) request).getRequest();
            }
        }
        if (multipartRequest == null) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "commons UPLOADER requires configuration of servlet filter 'org.primefaces.webapp.filter.FileUploadFilter'");
            }
        }
        return multipartRequest;
    }

    @Override
    public String getUploadDirectory(MultipartRequest request) {
        File uploadDir = request.getUploadDirectory();
        if (uploadDir == null) {
            return super.getUploadDirectory(request);
        }
        return uploadDir.getAbsolutePath();
    }
}
