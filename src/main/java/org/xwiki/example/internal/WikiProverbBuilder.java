/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.example.internal;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.wiki.WikiComponent;
import org.xwiki.component.wiki.WikiComponentBuilder;
import org.xwiki.component.wiki.WikiComponentException;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
@Singleton
@Named("proverb")
public class WikiProverbBuilder implements WikiComponentBuilder
{
    @Inject
    private Execution execution;

    @Inject
    private QueryManager queryManager;

    @Inject
    private EntityReferenceSerializer<String> serializer;

    @Override
    public List<DocumentReference> getDocumentReferences()
    {
        List<DocumentReference> references = new ArrayList<DocumentReference>();

        try {
            Query query =
                queryManager.createQuery("select doc.space, doc.name from Document doc, doc.object(XWiki.Proverb) "
                    + "as proverb where proverb.proverb <> ''",
                    Query.XWQL);
            List<Object[]> results = query.execute();
            for (Object[] result : results) {
                references.add(
                    new DocumentReference(getXWikiContext().getDatabase(), (String) result[0], (String) result[1]));
            }
        } catch (Exception e) {
            // Fail "silently"
            e.printStackTrace();
        }

        return references;
    }

    @Override
    public List<WikiComponent> buildComponents(DocumentReference reference) throws WikiComponentException
    {
        List<WikiComponent> components = new ArrayList<WikiComponent>();
        DocumentReference proverbXClass = new DocumentReference(getXWikiContext().getDatabase(), "XWiki", "Proverb");

        try {
            XWikiDocument doc = getXWikiContext().getWiki().getDocument(reference, getXWikiContext());
            for (BaseObject obj : doc.getXObjects(proverbXClass)) {
                String roleHint = serializer.serialize(obj.getReference());
                components.add(new WikiProverb(reference, roleHint, obj.getStringValue("proverb")));
            }
        } catch (Exception e) {
            throw new WikiComponentException(String.format("Failed to build Proverb components from document [%s]",
                reference.toString()), e);
        }

        return components;
    }

    private XWikiContext getXWikiContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
    }
}
