import os
import shutil

def run():
    base = 'src/main/java/com/acme/sica'
    os.makedirs(f'{base}/model/persistence', exist_ok=True)
    os.makedirs(f'{base}/view', exist_ok=True)
    os.makedirs(f'{base}/controller', exist_ok=True)

    moves = [
        ('domain', 'model/domain'),
        ('usecase', 'model/usecase'),
        ('infrastructure/adapter/out/jdbc', 'model/persistence/jdbc'),
        ('infrastructure/db', 'model/persistence/db'),
        ('infrastructure/config', 'model/persistence/config'),
        ('infrastructure/audit', 'model/audit'),
        
        ('gui', 'view/gui'),
        ('infrastructure/adapter/in/dto', 'view/dto'),
        
        ('infrastructure/adapter/in/http', 'controller/http_handlers'),
        ('infrastructure/http', 'controller/router'),
        ('infrastructure/security', 'controller/security')
    ]

    for src, dst in moves:
        src_path = os.path.join(base, src)
        dst_path = os.path.join(base, dst)
        if os.path.exists(src_path):
            print(f"Moving {src_path} to {dst_path}")
            # Ensure parent of dst exists
            os.makedirs(os.path.dirname(dst_path), exist_ok=True)
            os.rename(src_path, dst_path)
    
    # Remove empty infrastructure dirs
    if os.path.exists(f'{base}/infrastructure/adapter/in'): os.rmdir(f'{base}/infrastructure/adapter/in')
    if os.path.exists(f'{base}/infrastructure/adapter/out'): os.rmdir(f'{base}/infrastructure/adapter/out')
    if os.path.exists(f'{base}/infrastructure/adapter'): os.rmdir(f'{base}/infrastructure/adapter')
    if os.path.exists(f'{base}/infrastructure'): os.rmdir(f'{base}/infrastructure')

    replacements = {
        'com.acme.sica.domain': 'com.acme.sica.model.domain',
        'com.acme.sica.usecase': 'com.acme.sica.model.usecase',
        'com.acme.sica.infrastructure.adapter.out.jdbc': 'com.acme.sica.model.persistence.jdbc',
        'com.acme.sica.infrastructure.db': 'com.acme.sica.model.persistence.db',
        'com.acme.sica.infrastructure.config': 'com.acme.sica.model.persistence.config',
        'com.acme.sica.infrastructure.audit': 'com.acme.sica.model.audit',
        
        'com.acme.sica.gui': 'com.acme.sica.view.gui',
        'com.acme.sica.infrastructure.adapter.in.dto': 'com.acme.sica.view.dto',
        
        'com.acme.sica.infrastructure.adapter.in.http': 'com.acme.sica.controller.http_handlers',
        'com.acme.sica.infrastructure.http': 'com.acme.sica.controller.router',
        'com.acme.sica.infrastructure.security': 'com.acme.sica.controller.security',
    }

    # also handle test folder moves
    test_base = 'src/test/java/com/acme/sica'
    if os.path.exists(test_base):
        os.makedirs(f'{test_base}/model', exist_ok=True)
        os.makedirs(f'{test_base}/controller', exist_ok=True)
        # currently tests are in 'shared/security' and 'visitas/'
        if os.path.exists(f'{test_base}/shared/security'):
            os.makedirs(f'{test_base}/controller/security', exist_ok=True)
            os.rename(f'{test_base}/shared/security', f'{test_base}/controller/security')
            os.rmdir(f'{test_base}/shared')
        if os.path.exists(f'{test_base}/visitas'):
            os.rename(f'{test_base}/visitas', f'{test_base}/model/usecase/visitas')
            replacements['com.acme.sica.shared.security'] = 'com.acme.sica.controller.security'
            replacements['com.acme.sica.visitas'] = 'com.acme.sica.model.usecase.visitas'
            
    for root, dirs, files in os.walk('src'):
        for file in files:
            if file.endswith('.java'):
                path = os.path.join(root, file)
                with open(path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                changed = False
                for old, new in replacements.items():
                    if old in content:
                        content = content.replace(old, new)
                        changed = True
                
                if changed:
                    with open(path, 'w', encoding='utf-8') as f:
                        f.write(content)

if __name__ == '__main__':
    run()
