package com.studio08.xbgamestream.Web;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.core.view.ViewCompat;
import com.applovin.sdk.AppLovinEventParameters;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.SlowScriptResponse;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: /app/base.apk/classes3.dex */
public final class BasicGeckoViewPrompt implements GeckoSession.PromptDelegate {
    protected static final String LOGTAG = "BasicGeckoViewPrompt";
    public int filePickerRequestCode = 1;
    private final Activity mActivity;
    private GeckoSession.PromptDelegate.FilePrompt mFilePrompt;
    private GeckoResult<GeckoSession.PromptDelegate.PromptResponse> mFileResponse;
    private int mFileType;

    public BasicGeckoViewPrompt(Activity activity) {
        this.mActivity = activity;
    }

    @Override // org.mozilla.geckoview.GeckoSession.PromptDelegate
    public GeckoResult<GeckoSession.PromptDelegate.PromptResponse> onAlertPrompt(GeckoSession geckoSession, GeckoSession.PromptDelegate.AlertPrompt alertPrompt) {
        Activity activity = this.mActivity;
        if (activity == null) {
            return GeckoResult.fromValue(alertPrompt.dismiss());
        }
        AlertDialog.Builder positiveButton = new AlertDialog.Builder(activity).setTitle(alertPrompt.title).setMessage(alertPrompt.message).setPositiveButton(17039370, (DialogInterface.OnClickListener) null);
        GeckoResult<GeckoSession.PromptDelegate.PromptResponse> geckoResult = new GeckoResult<>();
        createStandardDialog(positiveButton, alertPrompt, geckoResult).show();
        return geckoResult;
    }

    @Override // org.mozilla.geckoview.GeckoSession.PromptDelegate
    public GeckoResult<GeckoSession.PromptDelegate.PromptResponse> onButtonPrompt(GeckoSession geckoSession, final GeckoSession.PromptDelegate.ButtonPrompt buttonPrompt) {
        Activity activity = this.mActivity;
        if (activity == null) {
            return GeckoResult.fromValue(buttonPrompt.dismiss());
        }
        AlertDialog.Builder message = new AlertDialog.Builder(activity).setTitle(buttonPrompt.title).setMessage(buttonPrompt.message);
        final GeckoResult<GeckoSession.PromptDelegate.PromptResponse> geckoResult = new GeckoResult<>();
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Web.BasicGeckoViewPrompt.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == -1) {
                    geckoResult.complete(buttonPrompt.confirm(0));
                } else if (i == -2) {
                    geckoResult.complete(buttonPrompt.confirm(2));
                } else {
                    geckoResult.complete(buttonPrompt.dismiss());
                }
            }
        };
        message.setPositiveButton(17039370, onClickListener);
        message.setNegativeButton(17039360, onClickListener);
        createStandardDialog(message, buttonPrompt, geckoResult).show();
        return geckoResult;
    }

    @Override // org.mozilla.geckoview.GeckoSession.PromptDelegate
    public GeckoResult<GeckoSession.PromptDelegate.PromptResponse> onSharePrompt(GeckoSession geckoSession, GeckoSession.PromptDelegate.SharePrompt sharePrompt) {
        return GeckoResult.fromValue(sharePrompt.dismiss());
    }

    private int getViewPadding(AlertDialog.Builder builder) {
        TypedArray obtainStyledAttributes = builder.getContext().obtainStyledAttributes(new int[]{16843683});
        int dimensionPixelSize = obtainStyledAttributes.getDimensionPixelSize(0, 1);
        obtainStyledAttributes.recycle();
        return dimensionPixelSize;
    }

    private LinearLayout addStandardLayout(AlertDialog.Builder builder, String str, String str2) {
        ScrollView scrollView = new ScrollView(builder.getContext());
        LinearLayout linearLayout = new LinearLayout(builder.getContext());
        int viewPadding = getViewPadding(builder);
        int i = (str2 == null || str2.isEmpty()) ? viewPadding : 0;
        linearLayout.setOrientation(1);
        linearLayout.setPadding(viewPadding, i, viewPadding, i);
        scrollView.addView(linearLayout);
        builder.setTitle(str).setMessage(str2).setView(scrollView);
        return linearLayout;
    }

    private AlertDialog createStandardDialog(AlertDialog.Builder builder, final GeckoSession.PromptDelegate.BasePrompt basePrompt, final GeckoResult<GeckoSession.PromptDelegate.PromptResponse> geckoResult) {
        AlertDialog create = builder.create();
        create.setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.studio08.xbgamestream.Web.BasicGeckoViewPrompt.2
            @Override // android.content.DialogInterface.OnDismissListener
            public void onDismiss(DialogInterface dialogInterface) {
                if (basePrompt.isComplete()) {
                    return;
                }
                geckoResult.complete(basePrompt.dismiss());
            }
        });
        return create;
    }

    @Override // org.mozilla.geckoview.GeckoSession.PromptDelegate
    public GeckoResult<GeckoSession.PromptDelegate.PromptResponse> onTextPrompt(GeckoSession geckoSession, final GeckoSession.PromptDelegate.TextPrompt textPrompt) {
        Activity activity = this.mActivity;
        if (activity == null) {
            return GeckoResult.fromValue(textPrompt.dismiss());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LinearLayout addStandardLayout = addStandardLayout(builder, textPrompt.title, textPrompt.message);
        final EditText editText = new EditText(builder.getContext());
        editText.setText(textPrompt.defaultValue);
        addStandardLayout.addView(editText);
        final GeckoResult<GeckoSession.PromptDelegate.PromptResponse> geckoResult = new GeckoResult<>();
        builder.setNegativeButton(17039360, (DialogInterface.OnClickListener) null).setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Web.BasicGeckoViewPrompt.3
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                geckoResult.complete(textPrompt.confirm(editText.getText().toString()));
            }
        });
        createStandardDialog(builder, textPrompt, geckoResult).show();
        return geckoResult;
    }

    @Override // org.mozilla.geckoview.GeckoSession.PromptDelegate
    public GeckoResult<GeckoSession.PromptDelegate.PromptResponse> onAuthPrompt(GeckoSession geckoSession, final GeckoSession.PromptDelegate.AuthPrompt authPrompt) {
        final EditText editText;
        Activity activity = this.mActivity;
        if (activity == null) {
            return GeckoResult.fromValue(authPrompt.dismiss());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LinearLayout addStandardLayout = addStandardLayout(builder, authPrompt.title, authPrompt.message);
        final int i = authPrompt.authOptions.flags;
        int i2 = authPrompt.authOptions.level;
        if ((i & 8) == 0) {
            EditText editText2 = new EditText(builder.getContext());
            editText2.setHint(AppLovinEventParameters.USER_ACCOUNT_IDENTIFIER);
            editText2.setText(authPrompt.authOptions.username);
            addStandardLayout.addView(editText2);
            editText = editText2;
        } else {
            editText = null;
        }
        final EditText editText3 = new EditText(builder.getContext());
        editText3.setHint("password");
        editText3.setText(authPrompt.authOptions.password);
        editText3.setInputType(129);
        addStandardLayout.addView(editText3);
        if (i2 != 0) {
            ImageView imageView = new ImageView(builder.getContext());
            imageView.setImageResource(17301551);
            addStandardLayout.addView(imageView);
        }
        final GeckoResult<GeckoSession.PromptDelegate.PromptResponse> geckoResult = new GeckoResult<>();
        builder.setNegativeButton(17039360, (DialogInterface.OnClickListener) null).setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Web.BasicGeckoViewPrompt.4
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i3) {
                if ((i & 8) == 0) {
                    geckoResult.complete(authPrompt.confirm(editText.getText().toString(), editText3.getText().toString()));
                } else {
                    geckoResult.complete(authPrompt.confirm(editText3.getText().toString()));
                }
            }
        });
        createStandardDialog(builder, authPrompt, geckoResult).show();
        return geckoResult;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /app/base.apk/classes3.dex */
    public static class ModifiableChoice {
        public final GeckoSession.PromptDelegate.ChoicePrompt.Choice choice;
        public String modifiableLabel;
        public boolean modifiableSelected;

        public ModifiableChoice(GeckoSession.PromptDelegate.ChoicePrompt.Choice choice) {
            this.choice = choice;
            this.modifiableSelected = choice.selected;
            this.modifiableLabel = choice.label;
        }
    }

    private void addChoiceItems(int i, ArrayAdapter<ModifiableChoice> arrayAdapter, GeckoSession.PromptDelegate.ChoicePrompt.Choice[] choiceArr, String str) {
        String str2;
        int i2 = 0;
        if (i == 1) {
            int length = choiceArr.length;
            while (i2 < length) {
                arrayAdapter.add(new ModifiableChoice(choiceArr[i2]));
                i2++;
            }
            return;
        }
        int length2 = choiceArr.length;
        while (i2 < length2) {
            GeckoSession.PromptDelegate.ChoicePrompt.Choice choice = choiceArr[i2];
            ModifiableChoice modifiableChoice = new ModifiableChoice(choice);
            GeckoSession.PromptDelegate.ChoicePrompt.Choice[] choiceArr2 = choice.items;
            if (str != null && choiceArr2 == null) {
                modifiableChoice.modifiableLabel = str + modifiableChoice.modifiableLabel;
            }
            arrayAdapter.add(modifiableChoice);
            if (choiceArr2 != null) {
                if (i == 2 || i == 3) {
                    str2 = str != null ? str + '\t' : "\t";
                } else {
                    str2 = null;
                }
                addChoiceItems(i, arrayAdapter, choiceArr2, str2);
            }
            i2++;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onChoicePromptImpl(final GeckoSession geckoSession, String str, String str2, final int i, GeckoSession.PromptDelegate.ChoicePrompt.Choice[] choiceArr, final GeckoSession.PromptDelegate.ChoicePrompt choicePrompt, final GeckoResult<GeckoSession.PromptDelegate.PromptResponse> geckoResult) {
        AlertDialog alertDialog;
        Activity activity = this.mActivity;
        if (activity == null) {
            geckoResult.complete(choicePrompt.dismiss());
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        addStandardLayout(builder, str, str2);
        final ListView listView = new ListView(builder.getContext());
        if (i == 3) {
            listView.setChoiceMode(2);
        }
        final ArrayAdapter<ModifiableChoice> arrayAdapter = new ArrayAdapter<ModifiableChoice>(builder.getContext(), 17367043) { // from class: com.studio08.xbgamestream.Web.BasicGeckoViewPrompt.5
            private static final int TYPE_COUNT = 6;
            private static final int TYPE_GROUP = 3;
            private static final int TYPE_MENU_CHECK = 1;
            private static final int TYPE_MENU_ITEM = 0;
            private static final int TYPE_MULTIPLE = 5;
            private static final int TYPE_SEPARATOR = 2;
            private static final int TYPE_SINGLE = 4;
            private LayoutInflater mInflater;
            private View mSeparator;

            @Override // android.widget.BaseAdapter, android.widget.Adapter
            public int getViewTypeCount() {
                return 6;
            }

            @Override // android.widget.BaseAdapter, android.widget.Adapter
            public int getItemViewType(int i2) {
                ModifiableChoice item = getItem(i2);
                if (item.choice.separator) {
                    return 2;
                }
                if (i == 1) {
                    return item.modifiableSelected ? 1 : 0;
                }
                if (item.choice.items != null) {
                    return 3;
                }
                int i3 = i;
                if (i3 == 2) {
                    return 4;
                }
                if (i3 == 3) {
                    return 5;
                }
                throw new UnsupportedOperationException();
            }

            @Override // android.widget.BaseAdapter, android.widget.ListAdapter
            public boolean isEnabled(int i2) {
                int i3;
                ModifiableChoice item = getItem(i2);
                return (item.choice.separator || item.choice.disabled || (((i3 = i) == 2 || i3 == 3) && item.choice.items != null)) ? false : true;
            }

            @Override // android.widget.ArrayAdapter, android.widget.Adapter
            public View getView(int i2, View view, ViewGroup viewGroup) {
                int i3;
                int itemViewType = getItemViewType(i2);
                if (itemViewType == 2) {
                    if (this.mSeparator == null) {
                        View view2 = new View(getContext());
                        this.mSeparator = view2;
                        view2.setLayoutParams(new AbsListView.LayoutParams(-1, 2, itemViewType));
                        TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(new int[]{16843284});
                        this.mSeparator.setBackgroundResource(obtainStyledAttributes.getResourceId(0, 0));
                        obtainStyledAttributes.recycle();
                    }
                    return this.mSeparator;
                }
                if (itemViewType == 0) {
                    i3 = 17367043;
                } else if (itemViewType == 1) {
                    i3 = 17367045;
                } else if (itemViewType == 3) {
                    i3 = 17367042;
                } else if (itemViewType == 4) {
                    i3 = 17367055;
                } else if (itemViewType != 5) {
                    throw new UnsupportedOperationException();
                } else {
                    i3 = 17367056;
                }
                if (view == null) {
                    if (this.mInflater == null) {
                        this.mInflater = LayoutInflater.from(builder.getContext());
                    }
                    view = this.mInflater.inflate(i3, viewGroup, false);
                }
                ModifiableChoice item = getItem(i2);
                TextView textView = (TextView) view;
                textView.setEnabled(true ^ item.choice.disabled);
                textView.setText(item.modifiableLabel);
                if (view instanceof CheckedTextView) {
                    boolean z = item.modifiableSelected;
                    if (itemViewType == 5) {
                        listView.setItemChecked(i2, z);
                    } else {
                        ((CheckedTextView) view).setChecked(z);
                    }
                }
                return view;
            }
        };
        addChoiceItems(i, arrayAdapter, choiceArr, null);
        listView.setAdapter((ListAdapter) arrayAdapter);
        builder.setView(listView);
        if (i == 2 || i == 1) {
            final AlertDialog createStandardDialog = createStandardDialog(builder, choicePrompt, geckoResult);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.studio08.xbgamestream.Web.BasicGeckoViewPrompt.6
                @Override // android.widget.AdapterView.OnItemClickListener
                public void onItemClick(AdapterView<?> adapterView, View view, int i2, long j) {
                    GeckoSession.PromptDelegate.ChoicePrompt.Choice[] choiceArr2;
                    ModifiableChoice modifiableChoice = (ModifiableChoice) arrayAdapter.getItem(i2);
                    if (i == 1 && (choiceArr2 = modifiableChoice.choice.items) != null) {
                        createStandardDialog.setOnDismissListener(null);
                        createStandardDialog.dismiss();
                        BasicGeckoViewPrompt.this.onChoicePromptImpl(geckoSession, modifiableChoice.modifiableLabel, null, i, choiceArr2, choicePrompt, geckoResult);
                        return;
                    }
                    geckoResult.complete(choicePrompt.confirm(modifiableChoice.choice));
                    createStandardDialog.dismiss();
                }
            });
            alertDialog = createStandardDialog;
        } else if (i == 3) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.studio08.xbgamestream.Web.BasicGeckoViewPrompt.7
                @Override // android.widget.AdapterView.OnItemClickListener
                public void onItemClick(AdapterView<?> adapterView, View view, int i2, long j) {
                    ((ModifiableChoice) arrayAdapter.getItem(i2)).modifiableSelected = ((CheckedTextView) view).isChecked();
                }
            });
            builder.setNegativeButton(17039360, (DialogInterface.OnClickListener) null).setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Web.BasicGeckoViewPrompt.8
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i2) {
                    int count = arrayAdapter.getCount();
                    ArrayList arrayList = new ArrayList(count);
                    for (int i3 = 0; i3 < count; i3++) {
                        ModifiableChoice modifiableChoice = (ModifiableChoice) arrayAdapter.getItem(i3);
                        if (modifiableChoice.modifiableSelected) {
                            arrayList.add(modifiableChoice.choice.id);
                        }
                    }
                    geckoResult.complete(choicePrompt.confirm((String[]) arrayList.toArray(new String[arrayList.size()])));
                }
            });
            alertDialog = createStandardDialog(builder, choicePrompt, geckoResult);
        } else {
            throw new UnsupportedOperationException();
        }
        alertDialog.show();
    }

    @Override // org.mozilla.geckoview.GeckoSession.PromptDelegate
    public GeckoResult<GeckoSession.PromptDelegate.PromptResponse> onChoicePrompt(GeckoSession geckoSession, GeckoSession.PromptDelegate.ChoicePrompt choicePrompt) {
        GeckoResult<GeckoSession.PromptDelegate.PromptResponse> geckoResult = new GeckoResult<>();
        onChoicePromptImpl(geckoSession, choicePrompt.title, choicePrompt.message, choicePrompt.type, choicePrompt.choices, choicePrompt, geckoResult);
        return geckoResult;
    }

    private static int parseColor(String str, int i) {
        try {
            return Color.parseColor(str);
        } catch (IllegalArgumentException unused) {
            return i;
        }
    }

    @Override // org.mozilla.geckoview.GeckoSession.PromptDelegate
    public GeckoResult<GeckoSession.PromptDelegate.PromptResponse> onColorPrompt(GeckoSession geckoSession, final GeckoSession.PromptDelegate.ColorPrompt colorPrompt) {
        Activity activity = this.mActivity;
        if (activity == null) {
            return GeckoResult.fromValue(colorPrompt.dismiss());
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        addStandardLayout(builder, colorPrompt.title, null);
        final int parseColor = parseColor(colorPrompt.defaultValue, 0);
        final ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<Integer>(builder.getContext(), 17367043) { // from class: com.studio08.xbgamestream.Web.BasicGeckoViewPrompt.9
            private LayoutInflater mInflater;

            @Override // android.widget.BaseAdapter, android.widget.Adapter
            public int getViewTypeCount() {
                return 2;
            }

            @Override // android.widget.BaseAdapter, android.widget.Adapter
            public int getItemViewType(int i) {
                return getItem(i).intValue() == parseColor ? 1 : 0;
            }

            @Override // android.widget.ArrayAdapter, android.widget.Adapter
            public View getView(int i, View view, ViewGroup viewGroup) {
                if (this.mInflater == null) {
                    this.mInflater = LayoutInflater.from(builder.getContext());
                }
                int intValue = getItem(i).intValue();
                if (view == null) {
                    view = this.mInflater.inflate(intValue == parseColor ? 17367045 : 17367043, viewGroup, false);
                }
                view.setBackgroundResource(17301528);
                view.getBackground().setColorFilter(intValue, PorterDuff.Mode.MULTIPLY);
                return view;
            }
        };
        arrayAdapter.addAll(-48060, -3407872, -17613, -30720, -6697984, -10053376, -13388315, -16737844, -5609780, -1, -5592406, -11184811, Integer.valueOf((int) ViewCompat.MEASURED_STATE_MASK));
        ListView listView = new ListView(builder.getContext());
        listView.setAdapter((ListAdapter) arrayAdapter);
        builder.setView(listView);
        final GeckoResult<GeckoSession.PromptDelegate.PromptResponse> geckoResult = new GeckoResult<>();
        final AlertDialog createStandardDialog = createStandardDialog(builder, colorPrompt, geckoResult);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.studio08.xbgamestream.Web.BasicGeckoViewPrompt.10
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                geckoResult.complete(colorPrompt.confirm(String.format("#%06x", Integer.valueOf(((Integer) arrayAdapter.getItem(i)).intValue() & ViewCompat.MEASURED_SIZE_MASK))));
                createStandardDialog.dismiss();
            }
        });
        createStandardDialog.show();
        return geckoResult;
    }

    private static Date parseDate(SimpleDateFormat simpleDateFormat, String str, boolean z) {
        if (str != null) {
            try {
                if (!str.isEmpty()) {
                    return simpleDateFormat.parse(str);
                }
            } catch (ParseException unused) {
            }
        }
        if (z) {
            return new Date();
        }
        return null;
    }

    private static void setTimePickerTime(TimePicker timePicker, Calendar calendar) {
        timePicker.setHour(calendar.get(11));
        timePicker.setMinute(calendar.get(12));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void setCalendarTime(Calendar calendar, TimePicker timePicker) {
        calendar.set(11, timePicker.getHour());
        calendar.set(12, timePicker.getMinute());
    }

    /* JADX WARN: Removed duplicated region for block: B:37:0x0098  */
    /* JADX WARN: Removed duplicated region for block: B:40:0x00b2  */
    /* JADX WARN: Removed duplicated region for block: B:42:0x00bb  */
    /* JADX WARN: Removed duplicated region for block: B:56:0x00ea  */
    @Override // org.mozilla.geckoview.GeckoSession.PromptDelegate
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public org.mozilla.geckoview.GeckoResult<org.mozilla.geckoview.GeckoSession.PromptDelegate.PromptResponse> onDateTimePrompt(org.mozilla.geckoview.GeckoSession r17, final org.mozilla.geckoview.GeckoSession.PromptDelegate.DateTimePrompt r18) {
        /*
            Method dump skipped, instructions count: 333
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.studio08.xbgamestream.Web.BasicGeckoViewPrompt.onDateTimePrompt(org.mozilla.geckoview.GeckoSession, org.mozilla.geckoview.GeckoSession$PromptDelegate$DateTimePrompt):org.mozilla.geckoview.GeckoResult");
    }

    @Override // org.mozilla.geckoview.GeckoSession.PromptDelegate
    public GeckoResult<GeckoSession.PromptDelegate.PromptResponse> onFilePrompt(GeckoSession geckoSession, GeckoSession.PromptDelegate.FilePrompt filePrompt) {
        String str;
        Activity activity = this.mActivity;
        if (activity == null) {
            return GeckoResult.fromValue(filePrompt.dismiss());
        }
        String str2 = null;
        if (filePrompt.mimeTypes != null) {
            str = null;
            for (String str3 : filePrompt.mimeTypes) {
                String lowerCase = str3.trim().toLowerCase(Locale.ROOT);
                int length = lowerCase.length();
                int indexOf = lowerCase.indexOf(47);
                if (indexOf < 0) {
                    indexOf = length;
                }
                String substring = lowerCase.substring(0, indexOf);
                String substring2 = lowerCase.substring(Math.min(indexOf + 1, length));
                if (str2 == null) {
                    str2 = substring;
                } else if (!str2.equals(substring)) {
                    str2 = "*";
                }
                if (str == null) {
                    str = substring2;
                } else if (!str.equals(substring2)) {
                    str = "*";
                }
            }
        } else {
            str = null;
        }
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        StringBuilder sb = new StringBuilder();
        if (str2 == null) {
            str2 = "*";
        }
        intent.setType(sb.append(str2).append('/').append(str != null ? str : "*").toString());
        intent.addCategory("android.intent.category.OPENABLE");
        intent.putExtra("android.intent.extra.LOCAL_ONLY", true);
        if (filePrompt.type == 2) {
            intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
        }
        if (filePrompt.mimeTypes.length > 0) {
            intent.putExtra("android.intent.extra.MIME_TYPES", filePrompt.mimeTypes);
        }
        GeckoResult<GeckoSession.PromptDelegate.PromptResponse> geckoResult = new GeckoResult<>();
        try {
            this.mFileResponse = geckoResult;
            this.mFilePrompt = filePrompt;
            activity.startActivityForResult(intent, this.filePickerRequestCode);
            return geckoResult;
        } catch (ActivityNotFoundException e) {
            Log.e(LOGTAG, "Cannot launch activity", e);
            return GeckoResult.fromValue(filePrompt.dismiss());
        }
    }

    public void onFileCallbackResult(int i, Intent intent) {
        GeckoResult<GeckoSession.PromptDelegate.PromptResponse> geckoResult = this.mFileResponse;
        if (geckoResult == null) {
            return;
        }
        this.mFileResponse = null;
        GeckoSession.PromptDelegate.FilePrompt filePrompt = this.mFilePrompt;
        this.mFilePrompt = null;
        if (i != -1 || intent == null) {
            geckoResult.complete(filePrompt.dismiss());
            return;
        }
        Uri data = intent.getData();
        ClipData clipData = intent.getClipData();
        if (filePrompt.type == 1 || (filePrompt.type == 2 && clipData == null)) {
            geckoResult.complete(filePrompt.confirm(this.mActivity, data));
        } else if (filePrompt.type == 2) {
            if (clipData == null) {
                Log.w(LOGTAG, "No selected file");
                geckoResult.complete(filePrompt.dismiss());
                return;
            }
            int itemCount = clipData.getItemCount();
            ArrayList arrayList = new ArrayList(itemCount);
            for (int i2 = 0; i2 < itemCount; i2++) {
                arrayList.add(clipData.getItemAt(i2).getUri());
            }
            geckoResult.complete(filePrompt.confirm(this.mActivity, (Uri[]) arrayList.toArray(new Uri[arrayList.size()])));
        }
    }

    public void onPermissionPrompt(GeckoSession geckoSession, String str, final GeckoSession.PermissionDelegate.Callback callback) {
        Activity activity = this.mActivity;
        if (activity == null) {
            callback.reject();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(str).setNegativeButton(17039360, new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Web.BasicGeckoViewPrompt.13
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                callback.reject();
            }
        }).setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Web.BasicGeckoViewPrompt.12
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                callback.grant();
            }
        });
        builder.create().show();
    }

    public void onSlowScriptPrompt(GeckoSession geckoSession, String str, final GeckoResult<SlowScriptResponse> geckoResult) {
        Activity activity = this.mActivity;
        if (activity == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(str).setNegativeButton("Wait", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Web.BasicGeckoViewPrompt.15
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                geckoResult.complete(SlowScriptResponse.CONTINUE);
            }
        }).setPositiveButton("Stop", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Web.BasicGeckoViewPrompt.14
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                geckoResult.complete(SlowScriptResponse.STOP);
            }
        });
        builder.create().show();
    }

    private Spinner addMediaSpinner(Context context, ViewGroup viewGroup, GeckoSession.PermissionDelegate.MediaSource[] mediaSourceArr, final String[] strArr) {
        ArrayAdapter<GeckoSession.PermissionDelegate.MediaSource> arrayAdapter = new ArrayAdapter<GeckoSession.PermissionDelegate.MediaSource>(context, 17367048) { // from class: com.studio08.xbgamestream.Web.BasicGeckoViewPrompt.16
            private View convertView(int i, View view) {
                if (view != null) {
                    GeckoSession.PermissionDelegate.MediaSource item = getItem(i);
                    TextView textView = (TextView) view;
                    String[] strArr2 = strArr;
                    textView.setText(strArr2 != null ? strArr2[i] : item.name);
                }
                return view;
            }

            @Override // android.widget.ArrayAdapter, android.widget.Adapter
            public View getView(int i, View view, ViewGroup viewGroup2) {
                return convertView(i, super.getView(i, view, viewGroup2));
            }

            @Override // android.widget.ArrayAdapter, android.widget.BaseAdapter, android.widget.SpinnerAdapter
            public View getDropDownView(int i, View view, ViewGroup viewGroup2) {
                return convertView(i, super.getDropDownView(i, view, viewGroup2));
            }
        };
        arrayAdapter.setDropDownViewResource(17367049);
        arrayAdapter.addAll(mediaSourceArr);
        Spinner spinner = new Spinner(context);
        spinner.setAdapter((SpinnerAdapter) arrayAdapter);
        spinner.setSelection(0);
        viewGroup.addView(spinner);
        return spinner;
    }

    public void onMediaPrompt(GeckoSession geckoSession, String str, GeckoSession.PermissionDelegate.MediaSource[] mediaSourceArr, GeckoSession.PermissionDelegate.MediaSource[] mediaSourceArr2, String[] strArr, String[] strArr2, final GeckoSession.PermissionDelegate.MediaCallback mediaCallback) {
        Activity activity = this.mActivity;
        if (activity == null || (mediaSourceArr == null && mediaSourceArr2 == null)) {
            mediaCallback.reject();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LinearLayout addStandardLayout = addStandardLayout(builder, str, null);
        final Spinner addMediaSpinner = mediaSourceArr != null ? addMediaSpinner(builder.getContext(), addStandardLayout, mediaSourceArr, strArr) : null;
        final Spinner addMediaSpinner2 = mediaSourceArr2 != null ? addMediaSpinner(builder.getContext(), addStandardLayout, mediaSourceArr2, strArr2) : null;
        builder.setNegativeButton(17039360, (DialogInterface.OnClickListener) null).setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Web.BasicGeckoViewPrompt.17
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                Spinner spinner = addMediaSpinner;
                GeckoSession.PermissionDelegate.MediaSource mediaSource = spinner != null ? (GeckoSession.PermissionDelegate.MediaSource) spinner.getSelectedItem() : null;
                Spinner spinner2 = addMediaSpinner2;
                mediaCallback.grant(mediaSource, spinner2 != null ? (GeckoSession.PermissionDelegate.MediaSource) spinner2.getSelectedItem() : null);
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.studio08.xbgamestream.Web.BasicGeckoViewPrompt.18
            @Override // android.content.DialogInterface.OnDismissListener
            public void onDismiss(DialogInterface dialogInterface) {
                mediaCallback.reject();
            }
        });
        create.show();
    }

    public void onMediaPrompt(GeckoSession geckoSession, String str, GeckoSession.PermissionDelegate.MediaSource[] mediaSourceArr, GeckoSession.PermissionDelegate.MediaSource[] mediaSourceArr2, GeckoSession.PermissionDelegate.MediaCallback mediaCallback) {
        onMediaPrompt(geckoSession, str, mediaSourceArr, mediaSourceArr2, null, null, mediaCallback);
    }

    @Override // org.mozilla.geckoview.GeckoSession.PromptDelegate
    public GeckoResult<GeckoSession.PromptDelegate.PromptResponse> onPopupPrompt(GeckoSession geckoSession, GeckoSession.PromptDelegate.PopupPrompt popupPrompt) {
        return GeckoResult.fromValue(popupPrompt.confirm(AllowOrDeny.ALLOW));
    }
}
