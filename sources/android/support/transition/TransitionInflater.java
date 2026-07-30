package android.support.transition;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.support.annotation.NonNull;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v4.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.InflateException;
import android.view.ViewGroup;
import java.io.IOException;
import java.lang.reflect.Constructor;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* JADX INFO: loaded from: classes.dex */
public class TransitionInflater {
    private final Context mContext;
    private static final Class<?>[] CONSTRUCTOR_SIGNATURE = {Context.class, AttributeSet.class};
    private static final ArrayMap<String, Constructor> CONSTRUCTORS = new ArrayMap<>();

    private TransitionInflater(@NonNull Context context) {
        this.mContext = context;
    }

    public static TransitionInflater from(Context context) {
        return new TransitionInflater(context);
    }

    public Transition inflateTransition(int resource) {
        XmlResourceParser parser = this.mContext.getResources().getXml(resource);
        try {
            try {
                return createTransitionFromXml(parser, Xml.asAttributeSet(parser), null);
            } catch (IOException e) {
                throw new InflateException(parser.getPositionDescription() + ": " + e.getMessage(), e);
            } catch (XmlPullParserException e2) {
                throw new InflateException(e2.getMessage(), e2);
            }
        } finally {
            parser.close();
        }
    }

    public TransitionManager inflateTransitionManager(int resource, ViewGroup sceneRoot) {
        XmlResourceParser parser = this.mContext.getResources().getXml(resource);
        try {
            try {
                return createTransitionManagerFromXml(parser, Xml.asAttributeSet(parser), sceneRoot);
            } catch (IOException e) {
                InflateException ex = new InflateException(parser.getPositionDescription() + ": " + e.getMessage());
                ex.initCause(e);
                throw ex;
            } catch (XmlPullParserException e2) {
                InflateException ex2 = new InflateException(e2.getMessage());
                ex2.initCause(e2);
                throw ex2;
            }
        } finally {
            parser.close();
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:79:0x017f, code lost:
    
        return r2;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private android.support.transition.Transition createTransitionFromXml(org.xmlpull.v1.XmlPullParser r9, android.util.AttributeSet r10, android.support.transition.Transition r11) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /*
            Method dump skipped, instruction units count: 384
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.transition.TransitionInflater.createTransitionFromXml(org.xmlpull.v1.XmlPullParser, android.util.AttributeSet, android.support.transition.Transition):android.support.transition.Transition");
    }

    private Object createCustom(AttributeSet attrs, Class expectedType, String tag) {
        Object objNewInstance;
        Class<?> c;
        String className = attrs.getAttributeValue(null, "class");
        if (className == null) {
            throw new InflateException(tag + " tag must have a 'class' attribute");
        }
        try {
            synchronized (CONSTRUCTORS) {
                Constructor<?> constructor = CONSTRUCTORS.get(className);
                if (constructor == null && (c = this.mContext.getClassLoader().loadClass(className).asSubclass(expectedType)) != null) {
                    constructor = c.getConstructor(CONSTRUCTOR_SIGNATURE);
                    constructor.setAccessible(true);
                    CONSTRUCTORS.put(className, constructor);
                }
                objNewInstance = constructor.newInstance(this.mContext, attrs);
            }
            return objNewInstance;
        } catch (Exception e) {
            throw new InflateException("Could not instantiate " + expectedType + " class " + className, e);
        }
    }

    private void getTargetIds(XmlPullParser parser, AttributeSet attrs, Transition transition) throws XmlPullParserException, IOException {
        int depth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if ((type != 3 || parser.getDepth() > depth) && type != 1) {
                if (type == 2) {
                    String name = parser.getName();
                    if (name.equals("target")) {
                        TypedArray a = this.mContext.obtainStyledAttributes(attrs, Styleable.TRANSITION_TARGET);
                        int id = TypedArrayUtils.getNamedResourceId(a, parser, "targetId", 1, 0);
                        if (id != 0) {
                            transition.addTarget(id);
                        } else {
                            int id2 = TypedArrayUtils.getNamedResourceId(a, parser, "excludeId", 2, 0);
                            if (id2 != 0) {
                                transition.excludeTarget(id2, true);
                            } else {
                                String transitionName = TypedArrayUtils.getNamedString(a, parser, "targetName", 4);
                                if (transitionName != null) {
                                    transition.addTarget(transitionName);
                                } else {
                                    String transitionName2 = TypedArrayUtils.getNamedString(a, parser, "excludeName", 5);
                                    if (transitionName2 != null) {
                                        transition.excludeTarget(transitionName2, true);
                                    } else {
                                        String className = TypedArrayUtils.getNamedString(a, parser, "excludeClass", 3);
                                        if (className != null) {
                                            try {
                                                transition.excludeTarget((Class) Class.forName(className), true);
                                            } catch (ClassNotFoundException e) {
                                                a.recycle();
                                                throw new RuntimeException("Could not create " + className, e);
                                            }
                                        } else {
                                            String className2 = TypedArrayUtils.getNamedString(a, parser, "targetClass", 0);
                                            if (className2 != null) {
                                                transition.addTarget(Class.forName(className2));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        a.recycle();
                    } else {
                        throw new RuntimeException("Unknown scene name: " + parser.getName());
                    }
                }
            } else {
                return;
            }
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:20:0x0055, code lost:
    
        return r2;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private android.support.transition.TransitionManager createTransitionManagerFromXml(org.xmlpull.v1.XmlPullParser r8, android.util.AttributeSet r9, android.view.ViewGroup r10) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /*
            r7 = this;
            int r0 = r8.getDepth()
            r2 = 0
        L5:
            int r3 = r8.next()
            r4 = 3
            if (r3 != r4) goto L12
            int r4 = r8.getDepth()
            if (r4 <= r0) goto L55
        L12:
            r4 = 1
            if (r3 == r4) goto L55
            r4 = 2
            if (r3 != r4) goto L5
            java.lang.String r1 = r8.getName()
            java.lang.String r4 = "transitionManager"
            boolean r4 = r1.equals(r4)
            if (r4 == 0) goto L2a
            android.support.transition.TransitionManager r2 = new android.support.transition.TransitionManager
            r2.<init>()
            goto L5
        L2a:
            java.lang.String r4 = "transition"
            boolean r4 = r1.equals(r4)
            if (r4 == 0) goto L38
            if (r2 == 0) goto L38
            r7.loadTransition(r9, r8, r10, r2)
            goto L5
        L38:
            java.lang.RuntimeException r4 = new java.lang.RuntimeException
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "Unknown scene name: "
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.String r6 = r8.getName()
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.String r5 = r5.toString()
            r4.<init>(r5)
            throw r4
        L55:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.transition.TransitionInflater.createTransitionManagerFromXml(org.xmlpull.v1.XmlPullParser, android.util.AttributeSet, android.view.ViewGroup):android.support.transition.TransitionManager");
    }

    private void loadTransition(AttributeSet attrs, XmlPullParser parser, ViewGroup sceneRoot, TransitionManager transitionManager) throws Resources.NotFoundException {
        Transition transition;
        TypedArray a = this.mContext.obtainStyledAttributes(attrs, Styleable.TRANSITION_MANAGER);
        int transitionId = TypedArrayUtils.getNamedResourceId(a, parser, "transition", 2, -1);
        int fromId = TypedArrayUtils.getNamedResourceId(a, parser, "fromScene", 0, -1);
        Scene fromScene = fromId < 0 ? null : Scene.getSceneForLayout(sceneRoot, fromId, this.mContext);
        int toId = TypedArrayUtils.getNamedResourceId(a, parser, "toScene", 1, -1);
        Scene toScene = toId < 0 ? null : Scene.getSceneForLayout(sceneRoot, toId, this.mContext);
        if (transitionId >= 0 && (transition = inflateTransition(transitionId)) != null) {
            if (toScene == null) {
                throw new RuntimeException("No toScene for transition ID " + transitionId);
            }
            if (fromScene == null) {
                transitionManager.setTransition(toScene, transition);
            } else {
                transitionManager.setTransition(fromScene, toScene, transition);
            }
        }
        a.recycle();
    }
}
