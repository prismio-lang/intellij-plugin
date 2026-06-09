package io.prismio.template;

import com.intellij.icons.AllIcons;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;

public class PrismioTemplateGroupDescriptor implements FileTemplateGroupDescriptorFactory {
    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {

        FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("Prismio", AllIcons.Nodes.Class);

        group.addTemplate(new FileTemplateDescriptor("Regular", AllIcons.Nodes.Class));
        group.addTemplate(new FileTemplateDescriptor("Enum", AllIcons.Nodes.Enum));
        group.addTemplate(new FileTemplateDescriptor("Virtual", AllIcons.Nodes.ExceptionClass));
        group.addTemplate(new FileTemplateDescriptor("Struct", AllIcons.Nodes.AbstractClass));
        group.addTemplate(new FileTemplateDescriptor("Sealed", AllIcons.Nodes.Annotationtype));


        return group;
    }
}
